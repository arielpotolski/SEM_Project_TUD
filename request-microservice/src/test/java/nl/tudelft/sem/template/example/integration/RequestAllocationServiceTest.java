package nl.tudelft.sem.template.example.integration;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.example.authentication.AuthManager;
import nl.tudelft.sem.template.example.authentication.JwtTokenVerifier;
import nl.tudelft.sem.template.example.controllers.ApprovingRequestsController;
import nl.tudelft.sem.template.example.domain.Request;
import nl.tudelft.sem.template.example.domain.RequestRepository;
import nl.tudelft.sem.template.example.domain.Resource;
import nl.tudelft.sem.template.example.domain.ResourceResponseModel;
import nl.tudelft.sem.template.example.services.RequestAllocationService;
import org.eclipse.jetty.util.IO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.RequestMatcher;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@ExtendWith(SpringExtension.class)
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockTokenVerifier", "mockAuthenticationManager"})
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class RequestAllocationServiceTest {

    @MockBean
    private ApprovingRequestsController approvingRequestsController;

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

    @Autowired
    private ObjectMapper objectMapper;

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();


    /**
     * Don't need a specific token,so we can test with this setup.
     */
    @BeforeEach
    public void setup() {
        System.setOut(new PrintStream(outputStreamCaptor));

        server = MockRestServiceServer.createServer(restTemplate);
        requestAllocationService.setRestTemplate(restTemplate);

        when(mockAuthenticationManager.getNetId()).thenReturn("test");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("Alexander");
        when(mockJwtTokenVerifier.getRoleFromToken(anyString())).thenReturn("ROLE_FACULTY");
    }

    @AfterEach
    public void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    public void getFacultyUserFacultiesTest() {
        // Test without prior loading from the user microservice
        server.expect(manyTimes(), requestTo("http://localhost:8081/getUserFaculties"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n\"faculties\": \"[EWI]\"}", MediaType.APPLICATION_JSON));

        List<String> facultyUserFaculties = requestAllocationService.getFacultyUserFaculties("");

        assertThat(facultyUserFaculties).isEqualTo(List.of("EWI"));
    }

    @Test
    public void getFacultyUserFacultiesErrorTest() {
        server.expect(once(), requestTo("http://localhost:8081/getUserFaculties"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        List<String> facultyUserFaculties = requestAllocationService.getFacultyUserFaculties("");
        assertThat(facultyUserFaculties).isEqualTo(new ArrayList<>());
    }

    @Test
    public void getFacultyUserFacultiesRandomResult() {
        server.expect(once(), requestTo("http://localhost:8081/getUserFaculties"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\n\"faculties\": \"[a]\"}", MediaType.APPLICATION_JSON));

        List<String> facultyUserFaculties = requestAllocationService.getFacultyUserFaculties("");
        assertThat(facultyUserFaculties).isEqualTo(List.of("a"));
    }

    @Test
    public void getFacultyUserFacultiesNoneTest() {
        server.expect(once(), requestTo("http://localhost:8081/getUserFaculties"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(", ", MediaType.APPLICATION_JSON));

        //List<String> facultyUserFaculties = requestAllocationService.getFacultyUserFaculties("");
        assertThat(requestAllocationService.getFacultyUserFaculties("")).isEqualTo(new ArrayList<String>());
    }

    @Test
    public void getFacultyUserFacultiesExceptionThrown() {
        server.expect(once(), requestTo("http://localhost:8081/getUserFaculties"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withException(new IOException()));

        List<String> facultyUserFaculties = requestAllocationService.getFacultyUserFaculties("");
        assertThat(facultyUserFaculties).isEqualTo(new ArrayList<>());
        assertThat(facultyUserFaculties).isEqualTo(Collections.emptyList());
        assertThat(facultyUserFaculties).isNotNull();
        assertThat(facultyUserFaculties).isEmpty();
        String error = "error with post:org.springframework.web.client.ResourceAccessException: I/O error on POST request for \"http://localhost:8081/getUserFaculties\": null; nested exception is java.io.IOException";
        assertThat(outputStreamCaptor.toString().trim()).isEqualTo(error);
    }

    @Test
    public void getReservedResourceTest() throws JsonProcessingException {
        var resources = new ResourceResponseModel[] {
            new ResourceResponseModel("EWI", 3.0, 2.0, 2.0),
            new ResourceResponseModel("EWI", 1.0, 1.0, 2.0)};
        var resourcesString = objectMapper.writeValueAsString(resources);
        server.expect(manyTimes(), requestTo("http://localhost:8082/resources/availableUntil/2022-12-24/EWI"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(resourcesString, MediaType.APPLICATION_JSON));

        List<Resource> reservedResources = requestAllocationService
                .getReservedResource("EWI", LocalDate.parse("2022-12-24"), "");

        assertThat(reservedResources).isEqualTo(List.of(resources).stream()
                .map(x -> new Resource(x.getFacultyName(), x.getResourceCpu(),
                x.getResourceGpu(), x.getResourceMemory())).collect(Collectors.toList()));
    }

    @Test
    public void getReservedResourceExceptionTest() throws JsonProcessingException {
        server.expect(manyTimes(), requestTo("http://localhost:8082/resources/availableUntil/2022-12-24/EWI"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withException(new IOException()));

        List<Resource> reservedResources = requestAllocationService
                .getReservedResource("EWI", LocalDate.parse("2022-12-24"), "");

        assertThat(reservedResources).isEqualTo(new ArrayList<>());
        assertThat(reservedResources).isEmpty();
        String error = "Error getting reserved resources: I/O error on GET request for"
                + " \"http://localhost:8082/resources/availableUntil/2022-12-24/EWI\": null; nested exception is java.io.IOException";
        assertThat(outputStreamCaptor.toString().trim()).isEqualTo(error);
    }

    @Test
    public void sendRequestToClusterTest1() throws JsonProcessingException {
        server.expect(once(), requestTo("http://localhost:8082/request"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("true", MediaType.APPLICATION_JSON));

        String dateString = "2025-12-12";

        Request request = new Request(1L, "test", "name", "desc",
                "Cs", 2.0, 3.0, 1.0, true, LocalDate.parse(dateString));

        boolean b = requestAllocationService.sendRequestToCluster(request, "token");

        assertThat(b).isTrue();
    }

    @Test
    public void sendRequestToClusterTest2() throws JsonProcessingException {
        server.expect(once(), requestTo("http://localhost:8082/request"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("ok", MediaType.APPLICATION_JSON));

        String dateString = "2025-12-12";

        Request request = new Request(1L, "test", "name", "desc",
                "Cs", 2.0, 3.0, 1.0, true, LocalDate.parse(dateString));

        boolean b = requestAllocationService.sendRequestToCluster(request, "token");

        assertThat(b).isTrue();
    }

    @Test
    public void sendRequestToClusterTestException() {
        server.expect(once(), requestTo("http://localhost:8082/request"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withException(new IOException()));

        String dateString = "2025-12-12";

        Request request = new Request(1L, "test", "name", "desc",
                "Cs", 2.0, 3.0, 1.0, true, LocalDate.parse(dateString));

        boolean b = requestAllocationService.sendRequestToCluster(request, "token");

        assertThat(b).isFalse();
        String error = "error with post: org.springframework.web.client.ResourceAccessException: I/O error on POST request"
                + " for \"http://localhost:8082/request\": null; nested exception is java.io.IOException";
        assertThat(outputStreamCaptor.toString().trim()).isEqualTo(error);
    }

    @Test
    public void sendDeclinedRequestToUserService1() {

        server.expect(once(), requestTo("http://localhost:8081/notification"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer token"))
                .andRespond(withSuccess("ok", MediaType.APPLICATION_JSON));
        String dateString = "2025-12-12";

        Request request = new Request(1L, "test", "name", "desc",
                "Cs", 2.0, 3.0, 1.0, false, LocalDate.parse(dateString));

        boolean b = requestAllocationService.sendDeclinedRequestToUserService(request, "token");

        assertThat(b).isTrue();

    }


    @Test
    public void sendDeclinedRequestToUserServiceException() {
        server.expect(once(), requestTo("http://localhost:8081/notification"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withException(new IOException()));
        String dateString = "2025-12-12";

        Request request = new Request(1L, "test", "name", "desc",
                "Cs", 4.0, 3.0, 1.0, false, LocalDate.parse(dateString));

        boolean b = requestAllocationService.sendDeclinedRequestToUserService(request, "token");

        assertThat(b).isFalse();
        String error = "error with post: org.springframework.web.client.ResourceAccessException:"
                + " I/O error on POST request for \"http://localhost:8081/notification\": null; nested exception is java.io.IOException";
        assertThat(outputStreamCaptor.toString().trim()).isEqualTo(error);

    }

    @Test
    public void sendDeclinedRequestToUserService2() {
        server.expect(once(), requestTo("http://localhost:8081/notification"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("true", MediaType.APPLICATION_JSON));
        String dateString = "2025-12-12";

        Request request = new Request(1L, "test", "name", "desc",
                "Cs", 4.0, 3.0, 1.0, false, LocalDate.parse(dateString));

        boolean b = requestAllocationService.sendDeclinedRequestToUserService(request, "token");

        assertThat(b).isFalse();
    }



    @Test
    public void notEnoughCpuResourceForJobTest() throws JsonProcessingException {
        // reserved resources for 24th of December 2022 for ewi
        var resources = new ResourceResponseModel[] {
            new ResourceResponseModel("EWI", 3.0, 2.0, 2.0),
            new ResourceResponseModel("EWI", 1.0, 1.0, 2.0)};
        var resourcesString = objectMapper.writeValueAsString(resources);
        server.expect(manyTimes(), requestTo("http://localhost:8082/resources/availableUntil/2022-12-25/EWI"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(resourcesString, MediaType.APPLICATION_JSON));

        // request
        Request request = new Request(1L, "test", "name", "desc",
                "EWI", 2.0, 3.0, 1.0, true, LocalDate.parse("2022-12-25"));

        boolean b = requestAllocationService.enoughResourcesForJob(request, "token");

        assertThat(b).isFalse();
    }

    @Test
    public void notEnoughGpuResourceForJobTest() throws JsonProcessingException {
        // reserved resources for 24th of December 2022 for ewi
        var resources = new ResourceResponseModel[] {
            new ResourceResponseModel("EWI", 3.0, 2.0, 2.0),
            new ResourceResponseModel("EWI", 3.0, 3.0, 2.0)};
        var resourcesString = objectMapper.writeValueAsString(resources);
        server.expect(manyTimes(), requestTo("http://localhost:8082/resources/availableUntil/2022-12-25/EWI"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(resourcesString, MediaType.APPLICATION_JSON));

        // request
        Request request = new Request(1L, "test", "name", "desc",
                "EWI", 2.0, 2.0, 1.0, true, LocalDate.parse("2022-12-25"));

        boolean b = requestAllocationService.enoughResourcesForJob(request, "token");

        assertThat(b).isTrue();
    }

    @Test
    public void notEnoughMemoryResourceForJobTest() throws JsonProcessingException {
        // reserved resources for 24th of December 2022 for ewi
        var resources = new ResourceResponseModel[] {
            new ResourceResponseModel("EWI", 3.0, 2.0, 1.0),
            new ResourceResponseModel("EWI", 2.0, 1.0, 2.0)};
        var resourcesString = objectMapper.writeValueAsString(resources);
        server.expect(manyTimes(), requestTo("http://localhost:8082/resources/availableUntil/2022-12-25/EWI"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(resourcesString, MediaType.APPLICATION_JSON));

        // request
        Request request = new Request(1L, "test", "name", "desc",
                "EWI", 2.0, 1.0, 2.0, true, LocalDate.parse("2022-12-25"));

        boolean b = requestAllocationService.enoughResourcesForJob(request, "token");

        assertThat(b).isTrue();
    }

}