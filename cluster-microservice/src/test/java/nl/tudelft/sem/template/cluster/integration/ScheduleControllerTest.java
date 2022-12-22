package nl.tudelft.sem.template.cluster.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

/**
 * Tests for the schedule controller.
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockTokenVerifier", "mockAuthenticationManager"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class ScheduleControllerTest {

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
    private Node node2;
    private Node node3;

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
        node2 = new NodeBuilder()
                .setNodeCpuResourceCapacityTo(0.0)
                .setNodeGpuResourceCapacityTo(0.0)
                .setNodeMemoryResourceCapacityTo(0.0)
                .withNodeName("FacultyCentralCore")
                .foundAtUrl("/" + "TPM" + "/central-core")
                .byUserWithNetId("SYSTEM")
                .assignToFacultyWithId("TPM").constructNodeInstance();
        node3 = new NodeBuilder()
                .setNodeCpuResourceCapacityTo(0.0)
                .setNodeGpuResourceCapacityTo(0.0)
                .setNodeMemoryResourceCapacityTo(0.0)
                .withNodeName("FacultyCentralCore")
                .foundAtUrl("/" + "AE" + "/central-core")
                .byUserWithNetId("SYSTEM")
                .assignToFacultyWithId("AE").constructNodeInstance();
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
    public void postFacultiesTest() throws Exception {
        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        List<String> list = List.of("EWI", "TPM");

        ResultActions result = mockMvc.perform(post("/faculties")
                            .accept(MediaType.APPLICATION_JSON).content(JsonUtil.serialize(list))
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());

        String response = result.andReturn().getResponse().getContentAsString();

        assertThat(response).isEqualTo("Successfully acknowledged all existing faculties.");

        // check that nodes exist in database and only those
        assertThat(nodeRepository.existsByFacultyId("EWI")).isTrue();
        assertThat(nodeRepository.existsByFacultyId("TPM")).isTrue();
        assertThat(nodeRepository.count()).isEqualTo(2);

        // check that expected nodes have been persisted
        assertThat(nodeRepository.findByUrl("/EWI/central-core")).isEqualTo(node1);
        assertThat(nodeRepository.findByUrl("/TPM/central-core")).isEqualTo(node2);

        // check that updates do not overwrite existing faculty cores
        List<String> newFaculties = List.of("AE", "EWI");

        ResultActions resultNew = mockMvc.perform(post("/faculties")
                .accept(MediaType.APPLICATION_JSON).content(JsonUtil.serialize(newFaculties))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        resultNew.andExpect(status().isOk());

        String responseNew = resultNew.andReturn().getResponse().getContentAsString();

        assertThat(responseNew).isEqualTo("Successfully acknowledged all existing faculties.");

        // check that nodes exist in database and only those
        assertThat(nodeRepository.existsByFacultyId("EWI")).isTrue();
        assertThat(nodeRepository.existsByFacultyId("TPM")).isTrue();
        assertThat(nodeRepository.existsByFacultyId("AE")).isTrue();
        assertThat(nodeRepository.existsByFacultyId("IO")).isFalse();
        assertThat(nodeRepository.count()).isEqualTo(3);

        assertThat(nodeRepository.findByUrl("/EWI/central-core")).isEqualTo(node1);
        assertThat(nodeRepository.findByUrl("/TPM/central-core")).isEqualTo(node2);
        assertThat(nodeRepository.findByUrl("/AE/central-core")).isEqualTo(node3);
    }

    @Test
    public void getEmptyScheduleTest() throws Exception {
        ResultActions result = mockMvc.perform(get("/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo("[]");
    }

    @Test
    public void getOneItemScheduleTest() throws Exception {
        jobScheduleRepository.save(job);

        ResultActions result = mockMvc.perform(get("/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        String check = JsonUtil.serialize(List.of(job));

        assertThat(response).isEqualTo(check);
    }

    @Test
    public void getMultipleItemScheduleTest() throws Exception {
        jobScheduleRepository.save(job);
        jobScheduleRepository.save(job2);
        jobScheduleRepository.save(job3);
        jobScheduleRepository.save(job4);

        ResultActions result = mockMvc.perform(get("/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        String check = JsonUtil.serialize(List.of(job, job2, job3, job4));

        assertThat(response).isEqualTo(check);
    }

    @Test
    public void sendTooEarlyRequestTest() throws Exception {
        model.setPreferredCompletionDate(LocalDate.of(2022, 12, 12));
        String json = JsonUtil.serialize(model);
        ResultActions result = mockMvc.perform(post("/request")
                .accept(MediaType.APPLICATION_JSON).content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo("The requested job cannot require the cluster to compute it before "
                + this.dateProvider.getTomorrow() + ".");
    }

    @Test
    public void sendInvalidRequestTest() throws Exception {
        model.setRequiredCpu(0.0);
        String json = JsonUtil.serialize(model);
        ResultActions result = mockMvc.perform(post("/request")
                .accept(MediaType.APPLICATION_JSON).content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo("The requested job cannot require more GPU or memory than CPU.");
    }

    @Test
    public void sendTooGreedyRequestTest() throws Exception {
        model.setRequiredCpu(10.0);
        String json = JsonUtil.serialize(model);
        ResultActions result = mockMvc.perform(post("/request")
                .accept(MediaType.APPLICATION_JSON).content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isBadRequest());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo("The requested job requires more resources than are assigned "
                + "to the EWI faculty.");
    }

    @Test
    public void sendValidRequestTest() throws Exception {
        // nodes
        node1.setCpuResources(5.0);
        node1.setGpuResources(3.0);
        node1.setMemoryResources(2.0);
        node1.setFacultyId("EWI");
        nodeRepository.save(node1);
        node2.setCpuResources(3.0);
        node2.setMemoryResources(1.0);
        node2.setFacultyId("EWI");
        nodeRepository.save(node2);

        // request
        model.setRequiredCpu(5.0);
        model.setRequiredGpu(2.0);
        model.setRequiredMemory(1.0);
        String json = JsonUtil.serialize(model);
        ResultActions result = mockMvc.perform(post("/request")
                .accept(MediaType.APPLICATION_JSON).content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        // Assert
        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo("Successfully scheduled job.");
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

    // empty because it needs us to refactor the code to be able to insert a class here
    @Test
    public void getResourcesAssignedToAllNonEmptyTest() throws Exception {

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
        String response = result.andReturn().getResponse().getContentAsString();
        //assertThat(response).isEqualTo(JsonUtil.serialize()); // same as above
    }

    @Test
    public void getResourcesReservedPerFacultyPerDay() throws Exception {
        // check both url paths
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
    public void getResourcesReservedPerFacultyForGivenDay() throws Exception {
    }

    @Test
    public void getResourcesReservedForGivenFacultyPerDay() throws Exception {
    }

    @Test
    public void getResourcesReservedForGivenFacultyForGivenDay() throws Exception {
    }

}
