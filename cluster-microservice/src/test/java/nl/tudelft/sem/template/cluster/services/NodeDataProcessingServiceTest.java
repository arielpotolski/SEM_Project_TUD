package nl.tudelft.sem.template.cluster.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.template.cluster.domain.builders.NodeBuilder;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.services.NodeDataProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class NodeDataProcessingServiceTest {

    @Autowired
    private transient NodeDataProcessingService dataProcessingService;

    private Node node1;
    private Node node2;
    private Node node3;

    /**
     * Set up the tests.
     */
    @BeforeEach
    void setup() {
        this.dataProcessingService.deleteAllNodes();
        this.node1 = new NodeBuilder()
                .setNodeCpuResourceCapacityTo(0.0)
                .assignToFacultyWithId("EWI")
                .setNodeGpuResourceCapacityTo(0.0)
                .setNodeMemoryResourceCapacityTo(0.0)
                .withNodeName("FacultyCentralCore")
                .foundAtUrl("/" + "EWI" + "/central-core")
                .byUserWithNetId("SYSTEM")
                .constructNodeInstance();
        this.node2 = new NodeBuilder()
                .setNodeCpuResourceCapacityTo(0.0)
                .setNodeGpuResourceCapacityTo(0.0)
                .setNodeMemoryResourceCapacityTo(0.0)
                .withNodeName("FacultyCentralCore")
                .foundAtUrl("/" + "TPM" + "/central-core")
                .byUserWithNetId("SYSTEM")
                .assignToFacultyWithId("TPM").constructNodeInstance();
        this.node3 = new NodeBuilder()
                .setNodeCpuResourceCapacityTo(0.0)
                .setNodeGpuResourceCapacityTo(0.0)
                .setNodeMemoryResourceCapacityTo(0.0)
                .withNodeName("FacultyCentralCore")
                .foundAtUrl("/" + "AE" + "/central-core")
                .byUserWithNetId("SYSTEM")
                .assignToFacultyWithId("AE").constructNodeInstance();
    }


    @Test
    public void saveNodeTest() {
        this.dataProcessingService.save(this.node1);
        assertThat(this.dataProcessingService.getByUrl(node1.getUrl())).isEqualTo(node1);
    }

    @Test
    public void getNumberOfNodesInRepoTest() {
        assertThat(this.dataProcessingService.getNumberOfNodesInRepository()).isEqualTo(0);

        this.dataProcessingService.save(this.node1);
        assertThat(this.dataProcessingService.getNumberOfNodesInRepository()).isEqualTo(1);
    }

    @Test
    public void nodeNotFoundExistsByUrl() {
        assertThat(this.dataProcessingService.existsByUrl(this.node1.getUrl())).isFalse();
    }

    @Test
    public void nodeExistsByUrl() {
        this.dataProcessingService.save(this.node1);
        assertThat(this.dataProcessingService.existsByUrl(node1.getUrl())).isTrue();
    }

    @Test
    public void nodeNotFoundExistsByFacultyId() {
        assertThat(this.dataProcessingService.existsByFacultyId(this.node1.getFacultyId())).isFalse();
    }

    @Test
    public void nodeExistsByFacultyId() {
        this.dataProcessingService.save(this.node1);
        assertThat(this.dataProcessingService.existsByFacultyId(this.node1.getFacultyId())).isTrue();
    }

    @Test
    public void getByUrlTest() {
        this.dataProcessingService.save(this.node1);
        assertThat(this.dataProcessingService.getByUrl(this.node1.getUrl())).isEqualTo(this.node1);
    }

    @Test
    public void getByFacultyIdTest() {
        List<Node> foundNode = new ArrayList<>();
        foundNode.add(this.node1);
        this.dataProcessingService.save(this.node1);
        assertThat(this.dataProcessingService.getByFacultyId(this.node1.getFacultyId()))
                .isEqualTo(foundNode);
    }

    @Test
    public void getAllNodesEmptyRepoTest() {
        assertThat(this.dataProcessingService.getAllNodes()).isEqualTo(new ArrayList<>());
    }

    @Test
    public void getAllNodesTest() {
        this.dataProcessingService.save(this.node1);
        this.dataProcessingService.save(this.node2);
        this.dataProcessingService.save(this.node3);

        List<Node> nodes = new ArrayList<>();
        nodes.add(this.node1);
        nodes.add(this.node2);
        nodes.add(this.node3);

        assertThat(this.dataProcessingService.getAllNodes()).isEqualTo(nodes);
    }

    @Test
    public void deleteNodeTest() {
        this.dataProcessingService.save(this.node1);
        assertThat(this.dataProcessingService.getNumberOfNodesInRepository()).isEqualTo(1);

        this.dataProcessingService.deleteNode(this.node1);
        assertThat(this.dataProcessingService.getNumberOfNodesInRepository()).isEqualTo(0);
    }

    @Test
    public void deleteAllNodesTest() {
        this.dataProcessingService.save(this.node1);
        this.dataProcessingService.save(this.node2);
        this.dataProcessingService.save(this.node3);

        this.dataProcessingService.deleteAllNodes();
        assertThat(this.dataProcessingService.getNumberOfNodesInRepository()).isEqualTo(0);
    }

}
