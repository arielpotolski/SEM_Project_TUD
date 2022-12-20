package nl.tudelft.sem.template.cluster.cluster;

import static org.assertj.core.api.Assertions.assertThat;

import nl.tudelft.sem.template.cluster.domain.builders.NodeBuilder;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NodeTest {

    private Node node;

    /**
     * sets up the tests.
     */
    @BeforeEach
    public void setup() {
        this.node = new Node(3, 2, 3,
            "node1", "url1", "netid1");
        this.node.setFacultyId("ewi");
    }

    @Test
    public void constructorTestNotNull() {
        assertThat(this.node != null).isTrue();
    }

    @Test
    public void getGpuTest() {
        assertThat(this.node.getGpuResources()).isEqualTo(2);
    }

    @Test
    public void setGpuTest() {
        this.node.setGpuResources(3);
        assertThat(this.node.getGpuResources()).isEqualTo(3);
    }

    @Test
    public void getCpuTest() {
        assertThat(this.node.getCpuResources()).isEqualTo(3);
    }

    @Test
    public void setCpuTest() {
        this.node.setCpuResources(5);
        assertThat(this.node.getCpuResources()).isEqualTo(5);
    }

    @Test
    public void getMemoryTest() {
        assertThat(this.node.getMemoryResources()).isEqualTo(3);
    }

    @Test
    public void setMemoryTest() {
        this.node.setMemoryResources(4);
        assertThat(this.node.getMemoryResources()).isEqualTo(4);
    }

    @Test
    public void getNameTest() {
        assertThat(this.node.getName()).isEqualTo("node1");
    }

    @Test
    public void setNameTest() {
        this.node.setName("Node");
        assertThat(this.node.getName()).isEqualTo("Node");
    }

    @Test
    public void getUrlTest() {
        assertThat(this.node.getUrl()).isEqualTo("url1");
    }

    @Test
    public void setUrlTest() {
        this.node.setUrl("Url");
        assertThat(this.node.getUrl()).isEqualTo("Url");
    }

    @Test
    public void getUserNetIdTest() {
        assertThat(this.node.getUserNetId()).isEqualTo("netid1");
    }

    @Test
    public void getFacultyIdTest() {
        assertThat(this.node.getFacultyId()).isEqualTo("ewi");
    }

    @Test
    public void setFacultyIdTest() {
        this.node.setFacultyId("facId");
        assertThat(this.node.getFacultyId()).isEqualTo("facId");
    }

    @Test
    public void hasEnoughCpuNegativeGpuTest() {
        this.node.setGpuResources(-1);
        assertThat(this.node.hasEnoughCpu()).isEqualTo("None of the resources can be negative.");
    }

    @Test
    public void hasEnoughCpuNegativeCpuTest() {
        this.node.setCpuResources(-1);
        assertThat(this.node.hasEnoughCpu()).isEqualTo("None of the resources can be negative.");
    }

    @Test
    public void hasEnoughCpuNegativeMemoryTest() {
        this.node.setMemoryResources(-1);
        assertThat(this.node.hasEnoughCpu()).isEqualTo("None of the resources can be negative.");
    }

    @Test
    public void hasEnoughCpuLessThanGpuAndMemory() {
        this.node.setGpuResources(3.1);
        this.node.setMemoryResources(3.1);

        assertThat(this.node.hasEnoughCpu()).isEqualTo("The amount of CPU resources "
                + "should be at least as much as the amount of GPU resources and at least"
                + " as much as the amount of memory resources.");
    }

    @Test
    public void hasEnoughCpuLessThanGpu() {
        this.node.setGpuResources(3.1);

        assertThat(this.node.hasEnoughCpu()).isEqualTo("The amount of CPU resources "
                + "should be at least as much as the amount of GPU resources.");
    }

    @Test
    public void hasEnoughCpuLessThanMemory() {
        this.node.setMemoryResources(3.1);

        assertThat(this.node.hasEnoughCpu()).isEqualTo("The amount of CPU resources"
                + " should be at least as much as the amount of memory resources.");
    }

    @Test
    public void hasEnoughCpuTrue() {
        assertThat(this.node.hasEnoughCpu()).isEqualTo("Your node has been "
                + "successfully added.");
    }

    @Test
    public void intObjectReturnsFalseOnEqualsTest() {
        assertThat(this.node.equals(1)).isFalse();
    }

    @Test
    public void compareToNullObjectReturnsFalse() {
        assertThat(this.node.equals(null)).isFalse();
    }

    @Test
    public void sameNodeEqualsTestReturnTrue() {
        Node that = this.node;
        assertThat(this.node.equals(that)).isTrue();
    }

    @Test
    public void differentGpuEqualsTest() {
        Node that = new Node(3, 3, 3, "node1", "url1", "netid1");
        that.setFacultyId("ewi");
        assertThat(this.node.equals(that)).isFalse();
    }

    @Test
    public void differentCpuEqualsTest() {
        Node that = new Node(2, 2, 3, "node1", "url1", "netid1");
        that.setFacultyId("ewi");
        assertThat(this.node.equals(that)).isFalse();
    }

    @Test
    public void differentMemoryEqualsTest() {
        Node that = new Node(3, 2, 4, "node1", "url1", "netid1");
        that.setFacultyId("ewi");
        assertThat(this.node.equals(that)).isFalse();
    }

    @Test
    public void differentNameEqualsTest() {
        Node that = new Node(3, 2, 3, "node2", "url1", "netid1");
        that.setFacultyId("ewi");
        assertThat(this.node.equals(that)).isFalse();
    }

    @Test
    public void differentUrlEqualsTest() {
        Node that = new Node(3, 2, 3, "node1", "url2", "netid1");
        that.setFacultyId("ewi");
        assertThat(this.node.equals(that)).isFalse();
    }

    @Test
    public void differentNetIdEqualsTest() {
        Node that = new Node(3, 2, 3, "node1", "url1", "netid2");
        that.setFacultyId("ewi");
        assertThat(this.node.equals(that)).isFalse();
    }

    @Test
    public void differentFacultyIdEqualsTest() {
        Node that = new Node(3, 2, 3, "node1", "url1", "netid1");
        that.setFacultyId("ae");
        assertThat(this.node.equals(that)).isFalse();
    }

    @Test
    public void equalsTestTrue() {
        Node that = new Node(3, 2, 3, "node1", "url1", "netid1");
        that.setFacultyId("ewi");
        assertThat(this.node.equals(that)).isTrue();
    }

    @Test
    public void hashCodeTest() {
        Node that = new Node(3, 2, 3, "node1", "url1", "netid1");
        that.setFacultyId("ewi");
        assertThat(this.node.hashCode()).isEqualTo(that.hashCode());
    }

    @Test
    public void hashCodeFails() {
        Node that = new Node(2, 3, 3, "node2", "url1", "netid1");
        that.setFacultyId("ewi");
        assertThat(this.node.hashCode()).isNotEqualTo(that.hashCode());
    }
}