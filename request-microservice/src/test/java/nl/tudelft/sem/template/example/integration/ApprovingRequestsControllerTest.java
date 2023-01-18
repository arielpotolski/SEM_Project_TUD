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

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import nl.tudelft.sem.template.example.authentication.AuthManager;
import nl.tudelft.sem.template.example.authentication.JwtTokenVerifier;
import nl.tudelft.sem.template.example.domain.ClockUser;
import nl.tudelft.sem.template.example.domain.DateProvider;
import nl.tudelft.sem.template.example.domain.Request;
import nl.tudelft.sem.template.example.domain.RequestRepository;
import nl.tudelft.sem.template.example.domain.ResourceResponseModel;
import nl.tudelft.sem.template.example.integration.utils.JsonUtil;
import nl.tudelft.sem.template.example.services.RequestAllocationService;
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

    }

    @Test
    public void sendRequestWaitingApprovalTest() throws Exception {

        var resources = new ResourceResponseModel[]{
            new ResourceResponseModel("EWI", 3.0, 2.0, 2.0),
            new ResourceResponseModel("EWI", 1.0, 1.0, 2.0)};
        var resourcesString = JsonUtil.serialize(resources);

        // when asked for resources, enough will be available
        server.expect(manyTimes(), requestTo("http://localhost:8082/resources/availableUntil/2022-12-23/EWI"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(resourcesString, MediaType.APPLICATION_JSON));

        Clock clock = Clock.fixed(
                Instant.parse("2022-12-22T11:00:00.00Z"),
                ZoneId.of("UTC"));

        clockUser.setClock(clock);

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("netId", "test");
        jsonObject.put("name", "test");
        jsonObject.put("description", "test");
        jsonObject.put("faculty", "EWI");
        jsonObject.put("cpu", 2.0);
        jsonObject.put("gpu", 1.0);
        jsonObject.put("memory", 1.0);
        jsonObject.put("approved", false);                    // triggers waiting for approval
        jsonObject.put("preferredDate", "2022-12-23");        // not the day after

        ResultActions result = mockMvc.perform(post("/job/sendRequest")
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonObject.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("The request was sent. Now it is to be approved by faculty.");

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

}
