package nl.tudelft.sem.template.example.integration;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import nl.tudelft.sem.template.example.TokenRequestModel;
import nl.tudelft.sem.template.example.authentication.AuthManager;
import nl.tudelft.sem.template.example.authentication.JwtTokenVerifier;
import nl.tudelft.sem.template.example.controllers.JobRequestController;
import nl.tudelft.sem.template.example.domain.Request;
import nl.tudelft.sem.template.example.domain.RequestRepository;
import nl.tudelft.sem.template.example.services.RequestAllocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;



@SpringBootTest
@ExtendWith(SpringExtension.class)
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockTokenVerifier", "mockAuthenticationManager"})
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class RequestAllocationServiceTest {

    @MockBean
    private JobRequestController jobRequestController;

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

    private final transient RestTemplate restTemplate = new RestTemplate();

    private transient MockRestServiceServer server;


    /**
     * Don't need a specific token,so we can test with this setup.
     */
    @BeforeEach
    public void setup() {
        server = MockRestServiceServer.createServer(restTemplate);
        requestAllocationService.setRestTemplate(restTemplate);

        when(mockAuthenticationManager.getNetId()).thenReturn("test");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("Alexander");
        when(mockJwtTokenVerifier.getRoleFromToken(anyString())).thenReturn("ROLE_FACULTY");

    }

    @Test
    public void getFacultyUserFacultiesTest() {
        // Test without prior loading from the user microservice
        server.expect(manyTimes(), requestTo("http://localhost:8081/getUserFaculties"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("EWI", MediaType.APPLICATION_JSON));

        List<String> facultyUserFaculties = requestAllocationService.getFacultyUserFaculties("");

        assertThat(facultyUserFaculties).isEqualTo(List.of("EWI"));
    }

    @Test
    public void getFacultyUserFacultiesErrorTest() {
        server.expect(once(), requestTo("http://localhost:8081/getUserFaculties"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> {
            requestAllocationService.getFacultyUserFaculties("");
        }).isInstanceOf(AssertionError.class);

    }

    @Test
    public void getFacultyUserFacultiesNoneTest() {
        server.expect(once(), requestTo("http://localhost:8081/getUserFaculties"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(", ", MediaType.APPLICATION_JSON));

        List<String> facultyUserFaculties = requestAllocationService.getFacultyUserFaculties("");

        assertThat(facultyUserFaculties).isEqualTo(new ArrayList<>());
    }

    @Test
    public void getReservedResourceTest() {

        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();

        server.expect(manyTimes(), requestTo("http://localhost:8081/getUserFaculties"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("Cs", MediaType.APPLICATION_JSON));

        List<String> facultyUserFaculties = requestAllocationService.getFacultyUserFaculties("");

        assertThat(facultyUserFaculties).isEqualTo(new ArrayList<>());

    }

    @Test
    public void sendRequestToClusterTest() throws JsonProcessingException, ParseException {

        String dateString = "2025-12-12";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Request request = new Request(1L, "test", "name", "desc",
                "Cs", 2.0, 3.0, 1.0, false, simpleDateFormat.parse(dateString));

        boolean b = requestAllocationService.sendRequestToCluster(request);

        assertThat(b).isFalse();
    }

    @Test
    public void sendDeclinedRequestToUserService() throws ParseException {

        String dateString = "2025-12-12";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Request request = new Request(1L, "test", "name", "desc",
                "Cs", 2.0, 3.0, 1.0, false, simpleDateFormat.parse(dateString));

        boolean b = requestAllocationService.sendDeclinedRequestToUserService(request);

        assertThat(b).isFalse();

    }

    @Test
    public void notEnoughResourceForJobTest(){}

    @Test
    public void enoughResourcesForJobTest() {}

}