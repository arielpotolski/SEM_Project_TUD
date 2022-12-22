package nl.tudelft.sem.template.cluster.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import nl.tudelft.sem.template.cluster.authentication.AuthManager;
import nl.tudelft.sem.template.cluster.authentication.JwtTokenVerifier;
import nl.tudelft.sem.template.cluster.domain.builders.NodeBuilder;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.cluster.NodeRepository;
import nl.tudelft.sem.template.cluster.integration.utils.JsonUtil;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Tests for the node controller.
 */
@SpringBootTest
@ExtendWith(SpringExtension.class)
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockTokenVerifier", "mockAuthenticationManager"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class NodeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private transient JwtTokenVerifier mockJwtTokenVerifier;

    @Autowired
    private transient AuthManager mockAuthenticationManager;

    @Autowired
    private transient NodeRepository nodeRepository;

    private Node node1;
    private Node node2;
    private Node node3;

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

        result2.andExpect(status().isBadRequest());
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

        result4.andExpect(status().isBadRequest());
        String response4 = result4.andReturn().getResponse().getContentAsString();
        assertThat(response4).isEqualTo("The amount of CPU resources should be at least as much as the amount "
            + "of GPU resources.");

        node3.setMemoryResources(2.0);
        node3.setUrl("aa");

        ResultActions result5 = mockMvc.perform(post("/nodes/add")
            .accept(MediaType.APPLICATION_JSON).content(JsonUtil.serialize(node3))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result5.andExpect(status().isBadRequest());
        String response5 = result5.andReturn().getResponse().getContentAsString();
        assertThat(response5).isEqualTo("The amount of CPU resources should be at least as much as the amount of GPU"
            + " resources and at least as much as the amount of memory resources.");

        node3.setCpuResources(-1.0);
        node3.setUrl("aaa");

        ResultActions result6 = mockMvc.perform(post("/nodes/add")
            .accept(MediaType.APPLICATION_JSON).content(JsonUtil.serialize(node3))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result6.andExpect(status().isBadRequest());
        String response6 = result6.andReturn().getResponse().getContentAsString();
        assertThat(response6).isEqualTo("None of the resources can be negative.");

        node3.setCpuResources(1.5);
        node3.setUrl("aaaa");

        ResultActions result7 = mockMvc.perform(post("/nodes/add")
            .accept(MediaType.APPLICATION_JSON).content(JsonUtil.serialize(node3))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result7.andExpect(status().isBadRequest());
        String response7 = result7.andReturn().getResponse().getContentAsString();
        assertThat(response7).isEqualTo("The amount of CPU resources should be at least as much as the amount"
            + " of memory resources.");
    }

    @Test
    public void deleteAllNodesTestEmptyUrl() throws Exception {
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
    public void deleteAllNodesTestSlashUrl() throws Exception {
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.delete("/nodes/delete/")
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

    @Test
    public void scheduleNodeRemovalNodeNotFoundTest() throws Exception {
        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders
            .post("/nodes/delete/user/a")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isBadRequest());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo("Could not find the node to be deleted."
            + " Check if the url provided is correct.");
        assertThat(this.nodeRepository.count()).isEqualTo(0);
    }

    @Test
    public void scheduleNodeRemovalUserNotOwnerTest() throws Exception {
        this.node1.setUrl("a");
        this.nodeRepository.save(node1);

        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders
            .post("/nodes/delete/user/a")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isBadRequest());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo("You cannot remove nodes that"
            + " other users have contributed to the cluster.");
        assertThat(this.nodeRepository.count()).isEqualTo(1);
    }

    /**
     * Due to the nature of Spring, we could not create a test that tests the removal
     * of the node from the repo. You can test the correct behaviour manually, through postman.
     * This tests if the output message to the client was the expected one.
     *
     * @throws Exception throws exception if endpoint fails
     */
    @Test
    public void scheduleNodeRemovalRemoveOneNodeSuccessfully() throws Exception {
        this.node1.setUrl("a");
        this.nodeRepository.save(node1);

        when(this.mockAuthenticationManager.getNetId()).thenReturn(this.node1.getUserNetId());

        ResultActions result = this.mockMvc.perform(MockMvcRequestBuilders
            .post("/nodes/delete/user/a")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo("Your node will be removed at midnight.");
    }

}
