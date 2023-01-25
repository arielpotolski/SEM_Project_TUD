package nl.tudelft.sem.template.cluster.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.services.NodeContributionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class NodeContributionServiceTest {

    @Autowired
    private transient NodeContributionService nodeContributionService;

    private Node node1;
    private Node node2;

    @BeforeEach
    void setup() {
        this.nodeContributionService.emptyListOfNodesToBeRemoved();
        this.node1 = new Node(1.0, 1.0, 1.0, "node1", "url1", "userTest");
        this.node2 = new Node(2.0, 2.0, 2.0, "node2", "url2", "userTest");
    }

    @Test
    public void addNodeToBeRemovedTest() {
        assertThat(this.nodeContributionService.numberOfNodesToRemove()).isEqualTo(0);
        assertThat(this.nodeContributionService.addNodeToBeRemoved(node1)).isTrue();
        assertThat(this.nodeContributionService.numberOfNodesToRemove()).isEqualTo(1);
    }

    @Test
    public void noNodesToRemoveTest() {
        assertThat(this.nodeContributionService.getNodesToRemove()).isEqualTo(new ArrayList<>());
    }

    @Test
    public void getNodesToBeRemovedOneNodeTest() {
        this.nodeContributionService.addNodeToBeRemoved(node1);
        List<Node> list = new ArrayList<>();
        list.add(node1);

        assertThat(this.nodeContributionService.getNodesToRemove()).isEqualTo(list);
    }

    @Test
    public void getNodesToBeRemovedMultipleNodesTest() {
        this.nodeContributionService.addNodeToBeRemoved(node1);
        this.nodeContributionService.addNodeToBeRemoved(node2);
        List<Node> list = new ArrayList<>();
        list.add(node1);
        list.add(node2);

        assertThat(this.nodeContributionService.getNodesToRemove()).isEqualTo(list);
    }

}
