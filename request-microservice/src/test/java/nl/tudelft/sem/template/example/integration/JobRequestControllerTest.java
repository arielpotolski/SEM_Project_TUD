package nl.tudelft.sem.template.example.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.template.example.authentication.AuthManager;
import nl.tudelft.sem.template.example.authentication.JwtTokenVerifier;
import nl.tudelft.sem.template.example.domain.ApprovalInformation;
import nl.tudelft.sem.template.example.domain.Request;
import nl.tudelft.sem.template.example.domain.RequestRepository;
import nl.tudelft.sem.template.example.integration.utils.JsonUtil;
import nl.tudelft.sem.template.example.services.RequestAllocationService;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)

@AutoConfigureMockMvc
public class JobRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private transient JwtTokenVerifier mockJwtTokenVerifier;

    @Autowired
    private transient AuthManager mockAuthenticationManager;

    @Autowired
    private transient RequestRepository requestRepository;

    @Autowired
    private transient RequestAllocationService requestAllocationService;

    /**
     * Setup so we don't need specific token for authentication for testing.
     */
    @BeforeEach
    public void setup() {

        when(mockAuthenticationManager.getNetId()).thenReturn("test");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("Alexander");
        when(mockJwtTokenVerifier.getRoleFromToken(anyString())).thenReturn("ROLE_FACULTY");

    }

    @Test
    public void sendRequestTestInFacultyNull() throws Exception {

        Request request = new Request();

        ResultActions result = mockMvc.perform(post("/job/sendRequest")
                .accept(MediaType.APPLICATION_JSON).content(JsonUtil.serialize(request))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("You are not verified to send requests to this faculty");

    }

    @Test
    public void sendRequestForTodayNotApproved() throws Exception {

        LocalDate localDate = LocalDate.now();
        //Date d = new SimpleDateFormat("yyyy-MM-dd").parse(localDate.toString());

        JSONObject jsonObject = new JSONObject();

        String date = localDate.getYear() + "-" + localDate.getMonthValue() + "-" + localDate.getDayOfMonth();

        jsonObject.put("netId", "test");
        jsonObject.put("name", "test");
        jsonObject.put("description", "test");
        jsonObject.put("faculty", "AE");
        jsonObject.put("cpu", 2.0);
        jsonObject.put("gpu", 1.0);
        jsonObject.put("memory", 1.0);
        jsonObject.put("approved", true);
        jsonObject.put("faculty", date);
        jsonObject.put("id", 123L);

        ResultActions result = mockMvc.perform(post("/job/sendRequest")
                .accept(MediaType.APPLICATION_JSON).content(JsonUtil.serialize(jsonObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("You are not verified to send requests to this faculty");
    }

    @Test
    public void sendRequestTestNotInFaculty() throws Exception {

        String dateString = "2023-12-12";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Request request = new Request(123L, "Test", "Test", "Test", "AE",
                2.0, 1.0, 1.0, true, simpleDateFormat.parse(dateString));

        ResultActions result = mockMvc.perform(post("/job/sendRequest")
                .accept(MediaType.APPLICATION_JSON).content(JsonUtil.serialize(request))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("You are not verified to send requests to this faculty");

    }

    @Test
    public void sendRequestForTodayDate() throws Exception {

        LocalDate localDate = LocalDate.now();
        //Date d = new SimpleDateFormat("yyyy-MM-dd").parse(localDate.toString());

        JSONObject jsonObject = new JSONObject();

        String date = localDate.getYear() + "-" + localDate.getMonthValue() + "-" + localDate.getDayOfMonth();

        jsonObject.put("netId", "test");
        jsonObject.put("name", "test");
        jsonObject.put("description", "test");
        jsonObject.put("faculty", "test");
        jsonObject.put("cpu", 9.0);
        jsonObject.put("gpu", 5.0);
        jsonObject.put("memory", 11.0);
        jsonObject.put("approved", true);
        jsonObject.put("faculty", date);

        ResultActions result = mockMvc.perform(post("/job/sendRequest")
                .accept(MediaType.APPLICATION_JSON).content(JsonUtil.serialize(jsonObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        // Need to mock the faculty
        //assertThat(response).isEqualTo("You cannot send requests for the same day.");

    }

    @Test
    public void sendRequest5MinBeforeNextDayTest() throws Exception {

        LocalDate localDate = LocalDate.now();
        //Date d = new SimpleDateFormat("yyyy-MM-dd").parse(localDate.toString());

        JSONObject jsonObject = new JSONObject();

        String date = localDate.getYear() + "-" + localDate.getMonthValue() + "-" + localDate.getDayOfMonth();

        jsonObject.put("netId", "test");
        jsonObject.put("name", "test");
        jsonObject.put("description", "test");
        jsonObject.put("faculty", "test");
        jsonObject.put("cpu", 9.0);
        jsonObject.put("gpu", 5.0);
        jsonObject.put("memory", 11.0);
        jsonObject.put("approved", true);
        jsonObject.put("faculty", date);

        ResultActions result = mockMvc.perform(post("/job/sendRequest")
                .accept(MediaType.APPLICATION_JSON).content(JsonUtil.serialize(jsonObject))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();

        // Need to mock the faculty and date 5 min before
        //assertThat(response).isEqualTo("You cannot send requests 5 min before the following day.");


    }

    @Test
    public void sendRequestWaitingApproval(){}



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
    public void sendApprovalsTest() throws Exception {

        String dateString = "2025-12-12";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Request req1 = new Request(1L, "test", "name", "desc",
                "Cs", 2.0, 3.0, 1.0, false, simpleDateFormat.parse(dateString));

        Request req2 = new Request(2L, "test", "name", "desc",
                "Cs", 2.0, 3.0, 1.0, false, simpleDateFormat.parse(dateString));

        Request req3 = new Request(3L, "test", "name", "desc",
                "Cs", 2.0, 3.0, 1.0, false, simpleDateFormat.parse(dateString));

        List<Request> requests = new ArrayList<>();
        requests.add(req1);
        requests.add(req2);
        requests.add(req3);

        requestRepository.saveAll(requests);

        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(manyTimes(), requestTo("http://localhost:8081/getUserFaculties"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("Cs", MediaType.APPLICATION_JSON));

        //RequestRepository requestRepository = mock(RequestRepository.class);
        //RequestAllocationService requestAllocationService = mock(RequestAllocationService.class);
        //when(requestRepository.findAll()).thenReturn(requests);

        //when(requestAllocationService.getFacultyUserFaculties("")).thenReturn(List.of("Cs"));


        Long[] ids = {1L, 2L, 3L};
        ApprovalInformation approvalInformation = new ApprovalInformation();
        approvalInformation.setIds(ids);

        ResultActions result = mockMvc.perform(post("/job/sendApprovals")
                .accept(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(approvalInformation))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));


        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo("[]");


    }



}

