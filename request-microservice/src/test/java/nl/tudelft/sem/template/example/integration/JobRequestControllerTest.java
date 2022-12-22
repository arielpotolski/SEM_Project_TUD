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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import nl.tudelft.sem.template.example.authentication.AuthManager;
import nl.tudelft.sem.template.example.authentication.JwtTokenVerifier;
import nl.tudelft.sem.template.example.domain.ApprovalInformation;
import nl.tudelft.sem.template.example.domain.ClockUser;
import nl.tudelft.sem.template.example.domain.Request;
import nl.tudelft.sem.template.example.domain.RequestRepository;
import nl.tudelft.sem.template.example.integration.utils.JsonUtil;
import nl.tudelft.sem.template.example.services.RequestAllocationService;
import org.json.JSONArray;
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

    @Mock
    private transient RequestAllocationService requestAllocationService;

    @Mock
    private transient ClockUser clockUser;

    /**
     * Setup so we don't need specific token for authentication for testing.
     */
    @BeforeEach
    public void setup() {

        when(mockAuthenticationManager.getNetId()).thenReturn("test");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("test");
        when(mockAuthenticationManager.getRole()).thenReturn("FACULTY");
        when(mockJwtTokenVerifier.getRoleFromToken(anyString())).thenReturn("ROLE_FACULTY");
        when(requestAllocationService.getFacultyUserFaculties("MockedToken"))
                .thenReturn(List.of("EWI", "IO"));
        when(clockUser.getTimeLDT()).thenReturn(LocalDateTime.now());
        when(clockUser.getTimeLD()).thenReturn(LocalDate.now());

    }

    @Test
    public void sendRequestTestInFacultyNull() throws Exception {

        when(clockUser.getTimeLDT()).thenReturn(LocalDateTime.parse("2022-12-22T10:00:00"));
        when(clockUser.getTimeLD()).thenReturn(LocalDate.parse("2022-12-22"));

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("netId", "test");
        jsonObject.put("name", "test");
        jsonObject.put("description", "test");
        //jsonObject.put("faculty", "");
        jsonObject.put("cpu", 2.0);
        jsonObject.put("gpu", 1.0);
        jsonObject.put("memory", 1.0);
        jsonObject.put("approved", true);
        jsonObject.put("preferredDate", "2022-12-23");


        ResultActions result = mockMvc.perform(post("/job/sendRequest")
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonObject.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("You are not verified to send requests to this faculty");

    }

    // ??????????????????????????????????????
    @Test
    public void sendRequestForTodayNotApproved() throws Exception {

        when(clockUser.getTimeLDT()).thenReturn(LocalDateTime.parse("2022-12-22T10:00:00"));
        when(clockUser.getTimeLD()).thenReturn(LocalDate.parse("2022-12-22"));

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("netId", "test");
        jsonObject.put("name", "test");
        jsonObject.put("description", "test");
        jsonObject.put("faculty", "AE");
        jsonObject.put("cpu", 2.0);
        jsonObject.put("gpu", 1.0);
        jsonObject.put("memory", 1.0);
        jsonObject.put("approved", true);
        jsonObject.put("preferredDate", "2022-12-22");


        ResultActions result = mockMvc.perform(post("/job/sendRequest")
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonObject.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("You cannot send requests for the same day.");


    }

    @Test
    public void sendRequestTestNotInFaculty() throws Exception {

        when(clockUser.getTimeLDT()).thenReturn(LocalDateTime.parse("2022-12-22T10:00:00"));
        when(clockUser.getTimeLD()).thenReturn(LocalDate.parse("2022-12-22"));

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("netId", "test");
        jsonObject.put("name", "test");
        jsonObject.put("description", "test");
        jsonObject.put("faculty", "AE");
        jsonObject.put("cpu", 2.0);
        jsonObject.put("gpu", 1.0);
        jsonObject.put("memory", 1.0);
        jsonObject.put("approved", true);
        jsonObject.put("preferredDate", "2022-12-23");


        ResultActions result = mockMvc.perform(post("/job/sendRequest")
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonObject.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("You are not assigned to this faculty.");

    }

    // Check if same day means preferred date!!!!
    @Test
    public void sendRequestForTodayDate() throws Exception {

        when(clockUser.getTimeLDT()).thenReturn(LocalDateTime.parse("2022-12-22T10:00:00"));
        when(clockUser.getTimeLD()).thenReturn(LocalDate.parse("2022-12-22"));

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("netId", "test");
        jsonObject.put("name", "test");
        jsonObject.put("description", "test");
        jsonObject.put("faculty", "EWI");
        jsonObject.put("cpu", 2.0);
        jsonObject.put("gpu", 1.0);
        jsonObject.put("memory", 1.0);
        jsonObject.put("approved", true);
        jsonObject.put("preferredDate", "2022-12-22");


        ResultActions result = mockMvc.perform(post("/job/sendRequest")
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonObject.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("You cannot send requests for the same day.");

    }

    @Test
    public void sendRequest5MinBeforeNextDayTest() throws Exception {

        when(clockUser.getTimeLDT()).thenReturn(LocalDateTime.parse("2022-12-22T23:55:00"));
        when(clockUser.getTimeLD()).thenReturn(LocalDate.parse("2022-12-22"));

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("netId", "test");
        jsonObject.put("name", "test");
        jsonObject.put("description", "test");
        jsonObject.put("faculty", "EWI");
        jsonObject.put("cpu", 2.0);
        jsonObject.put("gpu", 1.0);
        jsonObject.put("memory", 1.0);
        jsonObject.put("approved", true);
        jsonObject.put("preferredDate", "2022-12-23");


        ResultActions result = mockMvc.perform(post("/job/sendRequest")
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonObject.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("You cannot send requests 5 min before the following day.");


    }

    @Test
    public void sendRequestLessThan5MinBeforeNextDayTest() throws Exception {

        when(clockUser.getTimeLDT()).thenReturn(LocalDateTime.parse("2022-12-22T23:57:00"));
        when(clockUser.getTimeLD()).thenReturn(LocalDate.parse("2022-12-22"));

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("netId", "test");
        jsonObject.put("name", "test");
        jsonObject.put("description", "test");
        jsonObject.put("faculty", "EWI");
        jsonObject.put("cpu", 2.0);
        jsonObject.put("gpu", 1.0);
        jsonObject.put("memory", 1.0);
        jsonObject.put("approved", true);
        jsonObject.put("preferredDate", "2022-12-23");


        ResultActions result = mockMvc.perform(post("/job/sendRequest")
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonObject.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("You cannot send requests 5 min before the following day.");

    }

    @Test
    public void sendRequest459MinBeforeNextDayTest() throws Exception {

        when(clockUser.getTimeLDT()).thenReturn(LocalDateTime.parse("2022-12-22T23:55:01"));
        when(clockUser.getTimeLD()).thenReturn(LocalDate.parse("2022-12-22"));

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("netId", "test");
        jsonObject.put("name", "test");
        jsonObject.put("description", "test");
        jsonObject.put("faculty", "EWI");
        jsonObject.put("cpu", 2.0);
        jsonObject.put("gpu", 1.0);
        jsonObject.put("memory", 1.0);
        jsonObject.put("approved", true);
        jsonObject.put("preferredDate", "2022-12-23");


        ResultActions result = mockMvc.perform(post("/job/sendRequest")
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonObject.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("You cannot send requests 5 min before the following day.");
    }

    @Test
    public void sendRequest501MinBeforeNextDayTest() throws Exception {

        when(clockUser.getTimeLDT()).thenReturn(LocalDateTime.parse("2022-12-22T23:54:59"));
        when(clockUser.getTimeLD()).thenReturn(LocalDate.parse("2022-12-22"));

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("netId", "test");
        jsonObject.put("name", "test");
        jsonObject.put("description", "test");
        jsonObject.put("faculty", "EWI");
        jsonObject.put("cpu", 2.0);
        jsonObject.put("gpu", 1.0);
        jsonObject.put("memory", 1.0);
        jsonObject.put("approved", true);
        jsonObject.put("preferredDate", "2022-12-23");


        ResultActions result = mockMvc.perform(post("/job/sendRequest")
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonObject.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("The request is automatically forwarded and will be completed if there are sufficient resources");
    }

    @Test
    public void sendRequestExactly6HTest() throws Exception {

        when(clockUser.getTimeLDT()).thenReturn(LocalDateTime.parse("2022-12-22T18:00:00"));
        when(clockUser.getTimeLD()).thenReturn(LocalDate.parse("2022-12-22"));

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("netId", "test");
        jsonObject.put("name", "test");
        jsonObject.put("description", "test");
        jsonObject.put("faculty", "EWI");
        jsonObject.put("cpu", 2.0);
        jsonObject.put("gpu", 1.0);
        jsonObject.put("memory", 1.0);
        jsonObject.put("approved", true);
        jsonObject.put("preferredDate", "2022-12-23");


        ResultActions result = mockMvc.perform(post("/job/sendRequest")
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonObject.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("The request is automatically forwarded and will be completed if there are sufficient resources");

    }

    @Test
    public void sendRequests601HToMidnightTest() throws Exception {

        when(clockUser.getTimeLDT()).thenReturn(LocalDateTime.parse("2022-12-22T17:59:00"));
        when(clockUser.getTimeLD()).thenReturn(LocalDate.parse("2022-12-22"));

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("netId", "test");
        jsonObject.put("name", "test");
        jsonObject.put("description", "test");
        jsonObject.put("faculty", "EWI");
        jsonObject.put("cpu", 2.0);
        jsonObject.put("gpu", 1.0);
        jsonObject.put("memory", 1.0);
        jsonObject.put("approved", true);
        jsonObject.put("preferredDate", "2022-12-23");


        ResultActions result = mockMvc.perform(post("/job/sendRequest")
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonObject.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("The request was sent. Now it is to be approved by faculty.");
    }

    @Test
    public void sendRequests559ToMidnightTest() throws Exception {

        when(clockUser.getTimeLDT()).thenReturn(LocalDateTime.parse("2022-12-22T18:01:00"));
        when(clockUser.getTimeLD()).thenReturn(LocalDate.parse("2022-12-22"));

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("netId", "test");
        jsonObject.put("name", "test");
        jsonObject.put("description", "test");
        jsonObject.put("faculty", "EWI");
        jsonObject.put("cpu", 2.0);
        jsonObject.put("gpu", 1.0);
        jsonObject.put("memory", 1.0);
        jsonObject.put("approved", true);
        jsonObject.put("preferredDate", "2022-12-23");


        ResultActions result = mockMvc.perform(post("/job/sendRequest")
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonObject.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("The request is automatically forwarded and will be completed if there are sufficient resources");

    }

    @Test
    public void sendRequestLessThan6HTest() throws Exception {

        when(clockUser.getTimeLDT()).thenReturn(LocalDateTime.parse("2022-12-22T20:00:00"));
        when(clockUser.getTimeLD()).thenReturn(LocalDate.parse("2022-12-22"));

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("netId", "test");
        jsonObject.put("name", "test");
        jsonObject.put("description", "test");
        jsonObject.put("faculty", "EWI");
        jsonObject.put("cpu", 2.0);
        jsonObject.put("gpu", 1.0);
        jsonObject.put("memory", 1.0);
        jsonObject.put("approved", true);
        jsonObject.put("preferredDate", "2022-12-23");


        ResultActions result = mockMvc.perform(post("/job/sendRequest")
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonObject.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("The request is automatically forwarded and will be completed if there are sufficient resources");

    }


    // Mock the resources!!!!
    @Test
    public void sendRequestLessThan6HNoResourceTest() throws Exception {


        when(clockUser.getTimeLDT()).thenReturn(LocalDateTime.parse("2022-12-22T20:00:00"));
        when(clockUser.getTimeLD()).thenReturn(LocalDate.parse("2022-12-22"));

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("netId", "test");
        jsonObject.put("name", "test");
        jsonObject.put("description", "test");
        jsonObject.put("faculty", "EWI");
        jsonObject.put("cpu", 2.0);
        jsonObject.put("gpu", 1.0);
        jsonObject.put("memory", 1.0);
        jsonObject.put("approved", true);
        jsonObject.put("preferredDate", "2022-12-23");


        ResultActions result = mockMvc.perform(post("/job/sendRequest")
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonObject.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Request forwarded, but resources are insufficient or preferred date is not tomorrow");

    }

    @Test
    public void sendRequestLessThan6HTomorrowNotPreferredTest() throws Exception {

        when(clockUser.getTimeLDT()).thenReturn(LocalDateTime.parse("2022-12-22T20:00:00"));
        when(clockUser.getTimeLD()).thenReturn(LocalDate.parse("2022-12-22"));

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("netId", "test");
        jsonObject.put("name", "test");
        jsonObject.put("description", "test");
        jsonObject.put("faculty", "EWI");
        jsonObject.put("cpu", 2.0);
        jsonObject.put("gpu", 1.0);
        jsonObject.put("memory", 1.0);
        jsonObject.put("approved", true);
        jsonObject.put("preferredDate", "2022-12-25");        // not the day after


        ResultActions result = mockMvc.perform(post("/job/sendRequest")
                .accept(MediaType.APPLICATION_JSON)
                .content(jsonObject.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Request forwarded, but resources are insufficient or preferred date is not tomorrow");
    }

    @Test
    public void sendRequestWaitingApprovalTest() throws Exception {

        when(clockUser.getTimeLDT()).thenReturn(LocalDateTime.parse("2022-12-22T15:00:00"));
        when(clockUser.getTimeLD()).thenReturn(LocalDate.parse("2022-12-22"));

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("netId", "test");
        jsonObject.put("name", "test");
        jsonObject.put("description", "test");
        jsonObject.put("faculty", "EWI");
        jsonObject.put("cpu", 2.0);
        jsonObject.put("gpu", 1.0);
        jsonObject.put("memory", 1.0);
        jsonObject.put("approved", false);                    // triggers waiting for approval
        jsonObject.put("preferredDate", "2022-12-25");        // not the day after


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


    @Test
    public void sendApprovalsTest() throws Exception {

        String dateString = "2025-12-12";

        Request req1 = new Request(1L, "test", "test", "desc",
                "EWI", 2.0, 3.0, 1.0, false, LocalDate.parse(dateString));

        Request req2 = new Request(2L, "test", "test", "desc",
                "EWI", 2.0, 3.0, 1.0, false, LocalDate.parse(dateString));

        Request req3 = new Request(3L, "test", "test", "desc",
                "EWI", 2.0, 3.0, 1.0, false, LocalDate.parse(dateString));

        List<Request> requests = new ArrayList<>();
        requests.add(req1);
        requests.add(req2);
        requests.add(req3);

        requestRepository.saveAll(requests);

        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(manyTimes(), requestTo("http://localhost:8081/getUserFaculties"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("[EWI]", MediaType.APPLICATION_JSON));


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

