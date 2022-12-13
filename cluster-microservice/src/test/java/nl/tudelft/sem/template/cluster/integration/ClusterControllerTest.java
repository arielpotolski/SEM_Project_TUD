package nl.tudelft.sem.template.cluster.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
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
import nl.tudelft.sem.template.cluster.domain.cluster.FacultyTotalResources;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;
import nl.tudelft.sem.template.cluster.domain.cluster.JobScheduleRepository;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.cluster.NodeRepository;
import nl.tudelft.sem.template.cluster.domain.providers.DateProvider;
import nl.tudelft.sem.template.cluster.domain.services.NodeContributionService;
import nl.tudelft.sem.template.cluster.integration.utils.JsonUtil;
import nl.tudelft.sem.template.cluster.models.JobRequestModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.matchers.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Tests for the cluster controller.
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockTokenVerifier", "mockAuthenticationManager"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class ClusterControllerTest {

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
        when(mockAuthenticationManager.getNetId()).thenReturn("Alan&Ariel");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("Alan&Ariel");

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
        model.setPreferredCompletionDate(LocalDate.of(2022, 12, 15));
    }

    public boolean compareTwoDateFormats(String a, String b) {
        return false;
    }

    @Test
    public void getAllNodesTest() throws Exception {

        // Act
        // Still include Bearer token as AuthFilter itself is not mocked
        ResultActions result = mockMvc.perform(get("/nodes/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo("[]"); // empty list

        node1 = new NodeBuilder()
                .setNodeCpuResourceCapacityTo(2.0)
                .setNodeGpuResourceCapacityTo(1.0)
                .setNodeMemoryResourceCapacityTo(1.0)
                .withNodeName("MyFirstNode")
                .foundAtUrl("myUrl")
                .byUserWithNetId("Me")
                .assignToFacultyWithId("AE").constructNodeInstance();
        node2.setCpuResources(5.0);
        node2.setName("FUCK");

        // add some nodes
        nodeRepository.save(node1);
        nodeRepository.save(node2);

        // check all returned nodes
        ResultActions result2 = mockMvc.perform(get("/nodes")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result2.andExpect(status().isOk());
        String response2 = result2.andReturn().getResponse().getContentAsString();
        assertThat(response2).isEqualTo(JsonUtil.serialize(List.of(node1, node2))); // two elements list
        nodeRepository.delete(node2);

        // check all returned nodes
        ResultActions result3 = mockMvc.perform(get("/nodes")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result3.andExpect(status().isOk());
        String response3 = result3.andReturn().getResponse().getContentAsString();
        assertThat(response3).isEqualTo(JsonUtil.serialize(List.of(node1))); // one element list

        // TODO: generate random number of random nodes and check
    }

    @Test
    public void getNodeByUrlTest() throws Exception {
        ResultActions result = mockMvc.perform(get("/nodes/hackers")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isBadRequest());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo("");

        nodeRepository.save(node1);
        nodeRepository.save(node2);
        nodeRepository.save(node3);

        ResultActions result2 = mockMvc.perform(get("/nodes/TPM/central-core")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result2.andExpect(status().isOk());
        String response2 = result2.andReturn().getResponse().getContentAsString();
        assertThat(response2).isEqualTo(JsonUtil.serialize(List.of(node2)));
    }

    @Test
    public void addNodesTest() throws Exception {
        assertThat(nodeRepository.count()).isEqualTo(0); // no nodes

        // change node1
        node1.setCpuResources(3.0);
        node1.setMemoryResources(1.0);
        node1.setUrl("hippity");
        node1.setFacultyId(null);

        // add first node, check that Board of Examiners also added
        ResultActions result = mockMvc.perform(post("/nodes/add")
                .accept(MediaType.APPLICATION_JSON).content(JsonUtil.serialize(node1))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo("Your node has been successfully added.");

        assertThat(nodeRepository.count()).isEqualTo(2);
        assertThat(nodeRepository.existsByFacultyId("Board of Examiners")).isTrue();

        Node found = nodeRepository.findByUrl("hippity");
        assertThat(found).isNotNull();

        // add node with existing url
        ResultActions result2 = mockMvc.perform(post("/nodes/add")
                .accept(MediaType.APPLICATION_JSON).content(JsonUtil.serialize(node1))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result2.andExpect(status().isOk());
        String response2 = result2.andReturn().getResponse().getContentAsString();
        assertThat(response2).isEqualTo("Failed to add node. A node with this url already exists.");

        // add valid node to cluster
        ResultActions result3 = mockMvc.perform(post("/nodes/add")
                .accept(MediaType.APPLICATION_JSON).content(JsonUtil.serialize(node3))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result3.andExpect(status().isOk());
        String response3 = result3.andReturn().getResponse().getContentAsString();
        assertThat(response3).isEqualTo("Your node has been successfully added.");
        assertThat(nodeRepository.count()).isEqualTo(3);

        // try to add invalid node
        node3.setGpuResources(1.0);
        node3.setUrl("a");

        ResultActions result4 = mockMvc.perform(post("/nodes/add")
                .accept(MediaType.APPLICATION_JSON).content(JsonUtil.serialize(node3))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result4.andExpect(status().isOk());
        String response4 = result4.andReturn().getResponse().getContentAsString();
        assertThat(response4).isEqualTo("The amount of CPU resources should be at least as much as the amount "
                + "of GPU resources.");

        node3.setMemoryResources(2.0);
        node3.setUrl("aa");

        ResultActions result5 = mockMvc.perform(post("/nodes/add")
                .accept(MediaType.APPLICATION_JSON).content(JsonUtil.serialize(node3))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result5.andExpect(status().isOk());
        String response5 = result5.andReturn().getResponse().getContentAsString();
        assertThat(response5).isEqualTo("The amount of CPU resources should be at least as much as the amount of GPU"
                + " resources and at least as much as the amount of memory resources.");

        node3.setCpuResources(-1.0);
        node3.setUrl("aaa");

        ResultActions result6 = mockMvc.perform(post("/nodes/add")
                .accept(MediaType.APPLICATION_JSON).content(JsonUtil.serialize(node3))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result6.andExpect(status().isOk());
        String response6 = result6.andReturn().getResponse().getContentAsString();
        assertThat(response6).isEqualTo("None of the resources can be negative.");

        node3.setCpuResources(1.5);
        node3.setUrl("aaaa");

        ResultActions result7 = mockMvc.perform(post("/nodes/add")
                .accept(MediaType.APPLICATION_JSON).content(JsonUtil.serialize(node3))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result7.andExpect(status().isOk());
        String response7 = result7.andReturn().getResponse().getContentAsString();
        assertThat(response7).isEqualTo("The amount of CPU resources should be at least as much as the amount"
                + " of memory resources.");
    }

    @Test
    public void deleteAllNodesTest() throws Exception {
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/nodes/delete")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo("All nodes have been deleted from the cluster.");
        assertThat(nodeRepository.count()).isEqualTo(0);
    }

    @Test
    public void deleteNodeByUrlTestNoNodes() throws Exception {
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/nodes/delete/url")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo("Could not find the node to be deleted."
                + " Check if the url provided is correct.");
        assertThat(nodeRepository.count()).isEqualTo(0);
    }

    @Test
    public void deleteNodeByUrlTestWrongUrl() throws Exception {
        nodeRepository.save(node1);
        nodeRepository.save(node2);
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/nodes/delete/url")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo("Could not find the node to be deleted."
                + " Check if the url provided is correct.");
        assertThat(nodeRepository.count()).isEqualTo(2);
    }

    @Test
    public void deleteNodeByUrlTestCorrectUrl() throws Exception {
        nodeRepository.save(node1);
        nodeRepository.save(node2);
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/nodes/delete/EWI/central-core")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo("The node has been successfully deleted");
        assertThat(nodeRepository.count()).isEqualTo(1);
    }

    // TODO: split
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

    // commented out because it needs us to refactor the code to be able to insert a class here
    @Test
    public void getResourcesAssignedToAllNonEmptyTest() throws Exception {
//        // nodes
//        node1.setCpuResources(5.0);
//        node1.setGpuResources(3.0);
//        node1.setMemoryResources(2.0);
//        node1.setFacultyId("EWI");
//        nodeRepository.save(node1);
//        node2.setCpuResources(3.0);
//        node2.setMemoryResources(1.0);
//        node2.setFacultyId("EWI");
//        nodeRepository.save(node2);
//
//        ResultActions result = mockMvc.perform(get("/resources/assigned")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer MockedToken"));
//
//        result.andExpect(status().isOk());
//        String response = result.andReturn().getResponse().getContentAsString();
//        assertThat(response).isEqualTo("[{\"memory_Resources\":3.0,\"cpu_Resources\":8.0,\"gpu_Resources\":3.0," +
//                "\"faculty_Id\":\"EWI\"}]");
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
