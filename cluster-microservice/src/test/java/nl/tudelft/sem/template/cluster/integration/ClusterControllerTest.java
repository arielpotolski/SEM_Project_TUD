package nl.tudelft.sem.template.cluster.integration;

import nl.tudelft.sem.template.cluster.authentication.AuthManager;
import nl.tudelft.sem.template.cluster.authentication.JwtTokenVerifier;
import nl.tudelft.sem.template.cluster.domain.builders.NodeBuilder;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.cluster.NodeRepository;
import nl.tudelft.sem.template.cluster.integration.utils.JsonUtil;
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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

    @Test
    public void postFacultiesTest() throws Exception {

        when(mockAuthenticationManager.getNetId()).thenReturn("Admin");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("Admin");

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
        var node1 = new NodeBuilder()
                .setNodeCpuResourceCapacityTo(0.0)
                .setNodeGpuResourceCapacityTo(0.0)
                .setNodeMemoryResourceCapacityTo(0.0)
                .withNodeName("FacultyCentralCore")
                .foundAtUrl("/" + "EWI" + "/central-core")
                .byUserWithNetId("SYSTEM")
                .assignToFacultyWithId("EWI").constructNodeInstance();
        var node2 = new NodeBuilder()
                .setNodeCpuResourceCapacityTo(0.0)
                .setNodeGpuResourceCapacityTo(0.0)
                .setNodeMemoryResourceCapacityTo(0.0)
                .withNodeName("FacultyCentralCore")
                .foundAtUrl("/" + "TPM" + "/central-core")
                .byUserWithNetId("SYSTEM")
                .assignToFacultyWithId("TPM").constructNodeInstance();

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

        var node3 = new NodeBuilder()
                .setNodeCpuResourceCapacityTo(0.0)
                .setNodeGpuResourceCapacityTo(0.0)
                .setNodeMemoryResourceCapacityTo(0.0)
                .withNodeName("FacultyCentralCore")
                .foundAtUrl("/" + "AE" + "/central-core")
                .byUserWithNetId("SYSTEM")
                .assignToFacultyWithId("AE").constructNodeInstance();

        assertThat(nodeRepository.findByUrl("/EWI/central-core")).isEqualTo(node1);
        assertThat(nodeRepository.findByUrl("/TPM/central-core")).isEqualTo(node2);
        assertThat(nodeRepository.findByUrl("/AE/central-core")).isEqualTo(node3);
    }

}
