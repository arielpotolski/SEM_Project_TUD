package nl.tudelft.sem.template.cluster.cluster;

import static org.assertj.core.api.Assertions.assertThat;

import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NodeTest {

	private Node node;

	@BeforeEach
	public void setup() {
		this.node = new Node(2, 3, 3, "node1", "url1", "netid1");
	}

	@Test
	public void constructorTestNotNull() {
		assertThat(this.node != null).isTrue();
	}

	@Test
	public void getGpuTest() {
		assertThat(this.node.getGPUResources()).isEqualTo(2);
	}

	@Test
	public void setGpuTest() {
		this.node.changeGPUResources(3);
		assertThat(this.node.getGPUResources()).isEqualTo(3);
	}

	@Test
	public void getCpuTest() {
		assertThat(this.node.getCPUResources()).isEqualTo(3);
	}

	@Test
	public void setCpuTest() {
		this.node.changeCPUResources(5);
		assertThat(this.node.getCPUResources()).isEqualTo(5);
	}

	@Test
	public void getMemoryTest() {
		assertThat(this.node.getMemoryResources()).isEqualTo(3);
	}

	@Test
	public void setMemoryTest() {
		this.node.changeMemoryResources(4);
		assertThat(this.node.getMemoryResources()).isEqualTo(4);
	}

	@Test
	public void getNameTest() {
		assertThat(this.node.getNameOfNode()).isEqualTo("node1");
	}

	@Test
	public void setNameTest() {
		this.node.changeNameOfNode("Node");
		assertThat(this.node.getNameOfNode()).isEqualTo("Node");
	}

	@Test
	public void getUrlTest() {
		assertThat(this.node.getUrlOfNode()).isEqualTo("url1");
	}

	@Test
	public void setUrlTest() {
		this.node.changeUrlOfNode("Url");
		assertThat(this.node.getUrlOfNode()).isEqualTo("Url");
	}

	@Test
	public void getUserNetIdTest() {
		assertThat(this.node.getNodeOwnerUserNetId()).isEqualTo("netid1");
	}

	@Test
	public void setFacultyIdTest() {
		this.node.changeNodeAssignedFacultyId("facId");
		assertThat(this.node.getNodeAssignedFacultyId()).isEqualTo("facId");
	}

	@Test
	public void hasEnoughCpuNegativeGpuTest() {
		this.node.changeGPUResources(-1);
		assertThat(this.node.hasEnoughCPU()).isEqualTo("None of the resources can be negative.");
	}

	@Test
	public void hasEnoughCpuLessThanGpuAndMemory() {
		this.node.changeGPUResources(3.1);
		this.node.changeMemoryResources(3.1);

		assertThat(this.node.hasEnoughCPU()).isEqualTo("The amount of CPU resources " +
			"should be at least as much as the amount of GPU resources and at least" +
			" as much as the amount of memory resources.");
	}

	@Test
	public void hasEnoughCpuLessThanGpu() {
		this.node.changeGPUResources(3.1);

		assertThat(this.node.hasEnoughCPU()).isEqualTo("The amount of CPU resources " +
			"should be at least as much as the amount of GPU resources.");
	}

	@Test
	public void hasEnoughCpuLessThanMemory() {
		this.node.changeMemoryResources(3.1);

		assertThat(this.node.hasEnoughCPU()).isEqualTo("The amount of CPU resources" +
			" should be at least as much as the amount of memory resources.");
	}

	@Test
	public void hasEnoughCpuTrue() {
		assertThat(this.node.hasEnoughCPU()).isEqualTo("Your node has been " +
			"successfully added.");
	}

	@Test
	public void differentGpuEqualsTest() {
		Node that = new Node(3, 3, 3, "node1", "url1", "netid1");
		assertThat(this.node.equals(that)).isFalse();
	}

	@Test
	public void differentCpuEqualsTest() {
		Node that = new Node(2, 2, 3, "node1", "url1", "netid1");
		assertThat(this.node.equals(that)).isFalse();
	}

	@Test
	public void differentMemoryEqualsTest() {
		Node that = new Node(2, 3, 4, "node1", "url1", "netid1");
		assertThat(this.node.equals(that)).isFalse();
	}

	@Test
	public void differentNameEqualsTest() {
		Node that = new Node(2, 3, 3, "node2", "url1", "netid1");
		assertThat(this.node.equals(that)).isFalse();
	}

	@Test
	public void differentUrlEqualsTest() {
		Node that = new Node(2, 3, 3, "node1", "url2", "netid1");
		assertThat(this.node.equals(that)).isFalse();
	}

	@Test
	public void differentNetIdEqualsTest() {
		Node that = new Node(3, 3, 3, "node1", "url1", "netid2");
		assertThat(this.node.equals(that)).isFalse();
	}

	@Test
	public void EqualsTestTrue() {
		Node that = new Node(2, 3, 3, "node1", "url1", "netid1");
		assertThat(this.node.equals(that)).isTrue();
	}

	@Test
	public void HashCodeTest() {
		Node that = new Node(2, 3, 3, "node1", "url1", "netid1");
		assertThat(this.node.hashCode()).isEqualTo(that.hashCode());
	}

	@Test
	public void HashCodeFails() {
		Node that = new Node(2, 3, 3, "node2", "url1", "netid1");
		assertThat(this.node.hashCode()).isNotEqualTo(that.hashCode());
	}
}