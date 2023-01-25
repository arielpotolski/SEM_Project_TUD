package nl.tudelft.sem.template.cluster.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import nl.tudelft.sem.template.cluster.authentication.AuthManager;
import nl.tudelft.sem.template.cluster.authentication.JwtTokenVerifier;
import nl.tudelft.sem.template.cluster.domain.builders.JobBuilder;
import nl.tudelft.sem.template.cluster.domain.builders.NodeBuilder;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;
import nl.tudelft.sem.template.cluster.domain.cluster.JobScheduleRepository;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.cluster.NodeRepository;
import nl.tudelft.sem.template.cluster.domain.providers.DateProvider;
import nl.tudelft.sem.template.cluster.integration.utils.JsonUtil;
import nl.tudelft.sem.template.cluster.models.FacultyDatedResourcesResponseModel;
import nl.tudelft.sem.template.cluster.models.JobRequestModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@ExtendWith(SpringExtension.class)
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockTokenVerifier", "mockAuthenticationManager"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class FacultyResourcesInformationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private transient JwtTokenVerifier mockJwtTokenVerifier;

    @Autowired
    private transient AuthManager mockAuthenticationManager;

    @Autowired
    private transient NodeRepository nodeRepository;

    @Autowired
    private transient JobScheduleRepository jobScheduleRepository;

    @Autowired
    private transient DateProvider dateProvider;

    private Node node1;

    private Job job;
    private Job job2;
    private Job job3;
    private Job job4;

    private JobRequestModel model;

    /**
     * The setup of the tests.
     */
    @BeforeEach
    public void setup() {
        when(mockAuthenticationManager.getNetId()).thenReturn("ALAN");
        when(mockAuthenticationManager.getRole()).thenReturn("SYSADMIN");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("ALAN");
        when(mockJwtTokenVerifier.getRoleFromToken(anyString())).thenReturn("ROLE_SYSADMIN");

        node1 = new NodeBuilder()
            .setNodeCpuResourceCapacityTo(0.0)
            .setNodeGpuResourceCapacityTo(0.0)
            .setNodeMemoryResourceCapacityTo(0.0)
            .withNodeName("FacultyCentralCore")
            .foundAtUrl("/" + "EWI" + "/central-core")
            .byUserWithNetId("SYSTEM")
            .assignToFacultyWithId("EWI").constructNodeInstance();
        job = new JobBuilder().preferredCompletedBeforeDate(LocalDate.of(2022, 12, 15))
            .needingMemoryResources(1.0).needingGpuResources(1.0).needingCpuResources(5.0).withDescription("desc")
            .havingName("job").requestedByUserWithNetId("ALAN").requestedThroughFaculty("EWI")
            .constructJobInstance();
        job.setScheduledFor(LocalDate.of(2022, 12, 14));
        job2 = new JobBuilder().preferredCompletedBeforeDate(LocalDate.of(2022, 12, 15))
            .needingMemoryResources(0.0).needingGpuResources(1.0).needingCpuResources(2.0).withDescription("desc")
            .havingName("ob").requestedByUserWithNetId("ALAN").requestedThroughFaculty("AE")
            .constructJobInstance();
        job2.setScheduledFor(LocalDate.of(2022, 12, 14));
        job3 = new JobBuilder().preferredCompletedBeforeDate(LocalDate.of(2022, 12, 15))
            .needingMemoryResources(1.0).needingGpuResources(1.0).needingCpuResources(2.0).withDescription("desc")
            .havingName("job").requestedByUserWithNetId("ALAN").requestedThroughFaculty("EWI")
            .constructJobInstance();
        job3.setScheduledFor(LocalDate.of(2022, 12, 14));
        job4 = new JobBuilder().preferredCompletedBeforeDate(LocalDate.of(2022, 12, 17))
            .needingMemoryResources(1.0).needingGpuResources(1.0).needingCpuResources(2.0).withDescription("desc")
            .havingName("jb").requestedByUserWithNetId("ALAN").requestedThroughFaculty("EWI")
            .constructJobInstance();
        job4.setScheduledFor(LocalDate.of(2022, 12, 14));
        model = new JobRequestModel();
        model.setFacultyId("EWI");
        model.setJobName("name");
        model.setJobDescription("desc");
        model.setUserNetId("ALAN");
        model.setRequiredCpu(2.0);
        model.setRequiredGpu(1.0);
        model.setRequiredMemory(0.5);
        model.setPreferredCompletionDate(LocalDate.now().plusDays(2));
    }

    @Test
    public void getResourcesAssignedToAllEmptyTest() throws Exception {
        ResultActions result = mockMvc.perform(get("/resources/assigned")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo("[]");
    }

    @Test
    public void getResourcesAssignedToSpecificFacultyDoesNotExistTest() throws Exception {
        ResultActions result = mockMvc.perform(get("/resources/assigned/url")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isBadRequest());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo("");
    }

    @Test
    public void getResourcesAssignedToSpecificFacultyExistsTest() throws Exception {
        node1.setCpuResources(5.0);
        node1.setGpuResources(3.0);
        node1.setMemoryResources(2.0);
        node1.setFacultyId("EWI");
        nodeRepository.save(node1);

        ResultActions result = mockMvc.perform(get("/resources/assigned/EWI")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
    }

    @Test
    public void getResourcesReservedPerFacultyPerDayNoDayNoFacultySpecifiedExist() throws Exception {
        FacultyDatedResourcesResponseModel testingModel = new FacultyDatedResourcesResponseModel(
            LocalDate.of(2022, 12, 14), "EWI",
            5.0, 1.0, 1.0);

        jobScheduleRepository.save(job);
        ResultActions result = mockMvc.perform(get("/resources/reserved/")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo(JsonUtil.serialize(List.of(testingModel)));
    }

    @Test
    public void getResourcesReservedPerFacultyPerDayNoDaySpecifiedExist() throws Exception {
        FacultyDatedResourcesResponseModel testingModel = new FacultyDatedResourcesResponseModel(
            LocalDate.of(2022, 12, 14), "EWI",
            5.0, 1.0, 1.0);

        jobScheduleRepository.save(job);
        ResultActions result = mockMvc.perform(get("/resources/reserved/&EWI")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo(JsonUtil.serialize(List.of(testingModel)));
    }

    @Test
    public void getResourcesReservedPerFacultyPerDayNoDaySpecifiedFails() throws Exception {
        jobScheduleRepository.save(job);
        ResultActions result = mockMvc.perform(get("/resources/reserved/&AE")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void getResourcesReservedPerFacultyPerDayNoFacultySpecifiedExists() throws Exception {
        FacultyDatedResourcesResponseModel testingModel = new FacultyDatedResourcesResponseModel(
            LocalDate.of(2022, 12, 14), "EWI",
            5.0, 1.0, 1.0);

        jobScheduleRepository.save(job);
        ResultActions result = mockMvc.perform(get("/resources/reserved/2022-12-14&")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo(JsonUtil.serialize(List.of(testingModel)));
    }

    @Test
    public void getResourcesReservedPerFacultyPerDayNoFacultySpecifiedFails() throws Exception {
        jobScheduleRepository.save(job);
        ResultActions result = mockMvc.perform(get("/resources/reserved/2022-12-15&")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void getResourcesReservedPerFacultyPerDayFacultyAndDaySpecifiedExists() throws Exception {
        FacultyDatedResourcesResponseModel testingModel = new FacultyDatedResourcesResponseModel(
            LocalDate.of(2022, 12, 14), "EWI",
            5.0, 1.0, 1.0);

        jobScheduleRepository.save(job);
        ResultActions result = mockMvc.perform(get("/resources/reserved/2022-12-14&EWI")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo(JsonUtil.serialize(List.of(testingModel)));
    }

    @Test
    public void getResourcesReservedPerFacultyPerDayFacultyAndDaySpecifiedFailsDate() throws Exception {
        jobScheduleRepository.save(job);
        ResultActions result = mockMvc.perform(get("/resources/reserved/2022-12-15&EWI")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void getResourcesReservedPerFacultyPerDayFacultyAndDaySpecifiedFailsFaculty() throws Exception {
        jobScheduleRepository.save(job);
        ResultActions result = mockMvc.perform(get("/resources/reserved/2022-12-14&AE")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void getResourcesReservedPerFacultyPerDayFacultyAndDaySpecifiedFailsBoth() throws Exception {
        jobScheduleRepository.save(job);
        ResultActions result = mockMvc.perform(get("/resources/reserved/2022-12-14&AE")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void getAvailableResourcesPerFacultyPerDayExists() throws Exception {
        node1.setGpuResources(1);
        node1.setMemoryResources(1);
        node1.setCpuResources(5);
        nodeRepository.save(node1);

        FacultyDatedResourcesResponseModel testingModel = new FacultyDatedResourcesResponseModel(
            this.dateProvider.getTomorrow(), "EWI",
            5.0, 1.0, 1.0);

        ResultActions result = mockMvc.perform(get("/resources/available/")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo(JsonUtil.serialize(List.of(testingModel)));
    }

    @Test
    public void getAvailableResourcesPerFacultyForGivenDayExists() throws Exception {
        node1.setGpuResources(1);
        node1.setMemoryResources(1);
        node1.setCpuResources(5);
        nodeRepository.save(node1);

        FacultyDatedResourcesResponseModel testingModel = new FacultyDatedResourcesResponseModel(
            LocalDate.of(2022, 12, 12), "EWI",
            5.0, 1.0, 1.0);

        ResultActions result = mockMvc.perform(get("/resources/available/2022-12-12&")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo(JsonUtil.serialize(List.of(testingModel)));
    }

    @Test
    public void getAvailableResourcesPerDayForGivenFacultyFails() throws Exception {
        node1.setGpuResources(1);
        node1.setMemoryResources(1);
        node1.setCpuResources(5);
        nodeRepository.save(node1);


        ResultActions result = mockMvc.perform(get("/resources/available/&EWI")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void getAvailableResourcesPerDayForGivenFacultyExists() throws Exception {
        node1.setGpuResources(1);
        node1.setMemoryResources(1);
        node1.setCpuResources(5);
        nodeRepository.save(node1);

        this.job.setScheduledFor(this.dateProvider.getTomorrow());
        jobScheduleRepository.save(job);

        FacultyDatedResourcesResponseModel testingModel = new FacultyDatedResourcesResponseModel(
            this.dateProvider.getTomorrow(), "EWI",
            0.0, 0.0, 0.0);

        ResultActions result = mockMvc.perform(get("/resources/available/&EWI")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo(JsonUtil.serialize(List.of(testingModel)));
    }

    @Test
    public void getAvailableResourcesForGivenDayForGivenFacultyInvalidFaculty() throws Exception {
        node1.setGpuResources(1);
        node1.setMemoryResources(1);
        node1.setCpuResources(5);
        nodeRepository.save(node1);

        this.job.setScheduledFor(this.dateProvider.getTomorrow());
        jobScheduleRepository.save(job);

        ResultActions result = mockMvc.perform(get("/resources/available/2022-12-12&A")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isBadRequest());
    }

    @Test
    public void getAvailableResourcesForGivenDayForGivenFacultyExists() throws Exception {
        node1.setGpuResources(1);
        node1.setMemoryResources(1);
        node1.setCpuResources(5);
        nodeRepository.save(node1);

        this.job.setScheduledFor(this.dateProvider.getTomorrow());
        jobScheduleRepository.save(job);

        FacultyDatedResourcesResponseModel testingModel = new FacultyDatedResourcesResponseModel(
            LocalDate.of(2022, 12, 14), "EWI",
            5.0, 1.0, 1.0);

        ResultActions result = mockMvc.perform(get("/resources/available/2022-12-14&EWI")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo(JsonUtil.serialize(List.of(testingModel)));
    }

    @Test
    void getAvailableResourcesForGivenFacultyBeforeGivenDateIsInvalidDate() throws Exception {
        node1.setGpuResources(1);
        node1.setMemoryResources(1);
        node1.setCpuResources(5);
        nodeRepository.save(node1);

        this.job.setScheduledFor(this.dateProvider.getTomorrow());
        jobScheduleRepository.save(job);

        ResultActions result = mockMvc.perform(get("/resources/availableUntil/2022-12-14/EWI")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isBadRequest());
    }

    @Test
    void getAvailableResourcesForGivenFacultyBeforeGivenDateIsInvalidDateWordAsDate() throws Exception {
        node1.setGpuResources(1);
        node1.setMemoryResources(1);
        node1.setCpuResources(5);
        nodeRepository.save(node1);

        this.job.setScheduledFor(this.dateProvider.getTomorrow());
        jobScheduleRepository.save(job);

        ResultActions result = mockMvc.perform(get("/resources/availableUntil/twenty/EWI")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isBadRequest());
    }
}
