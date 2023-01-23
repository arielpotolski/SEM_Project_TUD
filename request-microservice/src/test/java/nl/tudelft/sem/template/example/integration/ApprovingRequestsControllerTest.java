package nl.tudelft.sem.template.example.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.h2.value.Value.JSON;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import nl.tudelft.sem.template.example.authentication.AuthManager;
import nl.tudelft.sem.template.example.authentication.JwtTokenVerifier;
import nl.tudelft.sem.template.example.controllers.ApprovingRequestsController;
import nl.tudelft.sem.template.example.domain.*;
import nl.tudelft.sem.template.example.integration.utils.JsonUtil;
import nl.tudelft.sem.template.example.services.RequestAllocationService;
import nl.tudelft.sem.template.example.util.Utils;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@ExtendWith(SpringExtension.class)
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockTokenVerifier", "mockAuthenticationManager"})
@DirtiesContext()

@AutoConfigureMockMvc
public class ApprovingRequestsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private transient JwtTokenVerifier mockJwtTokenVerifier;

    @Autowired
    private transient AuthManager mockAuthenticationManager;

    @Autowired
    private transient RequestRepository requestRepository;

    // THIS HAS TO BE AUTOWIRED, NOT MOCKED
    @Autowired
    private transient RequestAllocationService requestAllocationService;

    @Mock
    private transient DateProvider dateProvider;

    @Autowired
    private transient ClockUser clockUser;


    // WE HAVE TO USE THE SAME IN THE TEST AND IN THE ACTUAL CLASS
    private final transient RestTemplate restTemplate = new RestTemplate();

    // FOR CLIENT HTTP TESTING
    private transient MockRestServiceServer server;

    /**
     * Setup so we don't need specific token for authentication for testing.
     */
    @BeforeEach
    public void setup() {

        server = MockRestServiceServer.createServer(restTemplate); // bind the server to the template
        requestAllocationService.setRestTemplate(restTemplate);    // set the service to use the same template (!!!!!)

        // when asked for user faculties, requestAllocationService will return EWI and IO
        server.expect(manyTimes(), requestTo("http://localhost:8081/getUserFaculties"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n\"faculties\": \"[EWI, IO]\"}", MediaType.APPLICATION_JSON));

        when(mockAuthenticationManager.getNetId()).thenReturn("test");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("test");
        when(mockAuthenticationManager.getRole()).thenReturn("FACULTY");
        when(mockJwtTokenVerifier.getRoleFromToken(anyString())).thenReturn("ROLE_FACULTY");
        //when(requestAllocationService.enoughResourcesForJob(new ))

    }

    @Test
    public void sendRequestWaitingApprovalTest() throws Exception {

        var resources = new ResourceResponseModel[]{
            new ResourceResponseModel("EWI", 30.0, 20.0, 20.0),
            new ResourceResponseModel("EWI", 10.0, 10.0, 20.0)};
        var resourcesString = JsonUtil.serialize(resources);

        // when asked for resources, enough will be available
        server.expect(manyTimes(), requestTo("http://localhost:8082/resources/availableUntil/2022-12-23/EWI"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(resourcesString, MediaType.APPLICATION_JSON));

        server.expect(manyTimes(), requestTo("http://localhost:8082/request"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("Uspeh", MediaType.APPLICATION_JSON));

        Clock clock = Clock.fixed(
                Instant.parse("2022-12-22T11:00:00.00Z"),
                ZoneId.of("UTC"));

        clockUser.setClock(clock);
//
//        JSONObject jsonObject = new JSONObject();
//
//        jsonObject.put("netId", "test");
//        jsonObject.put("name", "test");
//        jsonObject.put("description", "test");
//        jsonObject.put("faculty", "EWI");
//        jsonObject.put("cpu", 2.0);
//        jsonObject.put("gpu", 1.0);
//        jsonObject.put("memory", 1.0);
//        jsonObject.put("approved", false);                    // triggers waiting for approval
//        jsonObject.put("preferredDate", "2022-12-23");        // not the day after
//
//        ResultActions result = mockMvc.perform(post("/job/sendRequests")
//                .accept(MediaType.APPLICATION_JSON)
//                .content(jsonObject.toString())
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer MockedToken"));
//
//        result.andExpect(status().isOk());
//        String response = result.andReturn().getResponse().getContentAsString();
//
//        assertThat(response).isEqualTo("The request was sent. Now it is to be approved by faculty.");

        ApprovalInformation approvalInformation = new ApprovalInformation();
        approvalInformation.setIds(new Long[]{1L, 2L});

        LocalDate ld = LocalDate.of(2022, 12, 23);


        Request req1 = new Request(1L, "test", "test",
                "desc", "EWI", 3.0, 2.0, 2.0, false, ld);

        Request req2 = new Request(2L, "test", "test",
                "desc", "EWI", 3.0, 2.0, 2.0, false, ld);

        Request req3 = new Request(3L, "test", "test",
                "desc", "EWI", 3.0, 2.0, 2.0, false, ld);

        requestRepository.save(req1);
        requestRepository.save(req2);
        requestRepository.save(req3);

        List<Request> all = requestRepository.findAll();


        JSONObject jonb = new JSONObject();
        jonb.put("ids", approvalInformation.getIds());


        ResultActions res = mockMvc.perform(post("/job/sendApprovals")
                .accept(MediaType.APPLICATION_JSON)
                .content(String.valueOf(jonb))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        System.out.println(5);

        String contentAsString = res.andReturn().getResponse().getContentAsString();

        assertThat(contentAsString).isEqualTo("[{\"id\":1,\"netId\":\"test\",\"name\":\"test\",\"description\":\"desc\",\"faculty\":\"EWI\",\"cpu\":3.0,\"gpu\":2.0,\"memory\":2.0,\"approved\":true,\"preferredDate\":\"2022-12-23\"},{\"id\":2,\"netId\":\"test\",\"name\":\"test\",\"description\":\"desc\",\"faculty\":\"EWI\",\"cpu\":3.0,\"gpu\":2.0,\"memory\":2.0,\"approved\":true,\"preferredDate\":\"2022-12-23\"}]");

    }


    // Can also add an assert if we change the request in the controller
    @Test
    public void pendingRequestsTest() throws Exception {

        ResultActions result = mockMvc.perform(get("/job/pendingRequests")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();

        // test when there are no loaded requests
        assertThat(response).isEqualTo("[]");

    }

    @Test
    public void pendingRequestsTestWithTests() throws Exception {

        LocalDate ld = LocalDate.of(2023, 11, 23);

        Request req1 = new Request(50L, "test", "test",
                "desc", "EWI", 3.0, 2.0, 2.0, false, ld);

        Request req2 = new Request(51L, "test", "test",
                "desc", "EWI", 3.0, 2.0, 2.0, false, ld);

        Request req3 = new Request(52L, "test", "test",
                "desc", "EWI", 3.0, 2.0, 2.0, false, ld);

        requestRepository.save(req1);
        requestRepository.save(req2);
        requestRepository.save(req3);

        List<Request> all = requestRepository.findAll();

        ResultActions result = mockMvc.perform(get("/job/pendingRequests")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();

        // test when there are no loaded requests
        assertThat(response).isEqualTo("[{\"id\":3,\"netId\":\"test\",\"name\":\"test\",\"description\":\"desc\",\"faculty\":\"EWI\",\"cpu\":3.0,\"gpu\":2.0,\"memory\":2.0,\"approved\":false,\"preferredDate\":\"2022-12-23\"},{\"id\":4,\"netId\":\"test\",\"name\":\"test\",\"description\":\"desc\",\"faculty\":\"EWI\",\"cpu\":3.0,\"gpu\":2.0,\"memory\":2.0,\"approved\":false,\"preferredDate\":\"2023-11-23\"},{\"id\":5,\"netId\":\"test\",\"name\":\"test\",\"description\":\"desc\",\"faculty\":\"EWI\",\"cpu\":3.0,\"gpu\":2.0,\"memory\":2.0,\"approved\":false,\"preferredDate\":\"2023-11-23\"},{\"id\":6,\"netId\":\"test\",\"name\":\"test\",\"description\":\"desc\",\"faculty\":\"EWI\",\"cpu\":3.0,\"gpu\":2.0,\"memory\":2.0,\"approved\":false,\"preferredDate\":\"2023-11-23\"}]");
    }


//    @Test
//    public void handleRequestTest1() throws Exception {
//
//        var resources = new ResourceResponseModel[]{
//                new ResourceResponseModel("EWI", 3.0, 2.0, 2.0),
//                new ResourceResponseModel("EWI", 1.0, 1.0, 2.0)};
//        var resourcesString = JsonUtil.serialize(resources);
//
//        // when asked for resources, enough will be available
//        server.expect(manyTimes(), requestTo("http://localhost:8082/resources/availableUntil/2022-12-23/EWI"))
//                .andExpect(method(HttpMethod.GET))
//                .andRespond(withSuccess(resourcesString, MediaType.APPLICATION_JSON));
//
//        Clock clock = Clock.fixed(
//                Instant.parse("2022-12-22T11:00:00.00Z"),
//                ZoneId.of("UTC"));
//
//        LocalDate ld = LocalDate.of(2023, 11, 23);
//
//        Request req1 = new Request(50L, "test", "test",
//                "desc", "EWI", 3.0, 2.0, 2.0, false, ld);
//
//        Request req2 = new Request(51L, "test", "test",
//                "desc", "EWI", 3.0, 2.0, 2.0, false, ld);
//
//        Request req3 = new Request(52L, "test", "test",
//                "desc", "EWI", 3.0, 2.0, 2.0, false, ld);
//
//        List<Request> all = requestRepository.findAll();
//
//        requestRepository.save(req1);
//        requestRepository.save(req2);
//        requestRepository.save(req3);
//
//        //handleRequests(all,"test");
//
//        //assertThat(response).isEqualTo("The request was sent. Now it is to be approved by faculty.");
//
//    }

}

