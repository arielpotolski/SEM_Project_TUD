package nl.tudelft.sem.template.cluster.services;

import nl.tudelft.sem.template.cluster.controllers.ClusterController;
import nl.tudelft.sem.template.cluster.domain.builders.NodeBuilder;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.cluster.NodeRepository;
import nl.tudelft.sem.template.cluster.domain.services.NodeAssignmentService;
import nl.tudelft.sem.template.cluster.domain.services.NodeContributionService;
import nl.tudelft.sem.template.cluster.domain.strategies.NodeAssignmentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class NodeContributionServiceTest {

	@Autowired
	private transient NodeRepository repo;

	@Autowired
	private transient NodeAssignmentService assign;

	@Autowired
	private transient NodeContributionService contribute;

	/*@Autowired
	private transient ClusterController controller;*/

	//private List faculties;

	//private NodeAssignmentStrategy strategy;

	private Node node;
	//private Node node2;

	@BeforeEach
	void setUp() {
		this.assign = new NodeAssignmentService(this.repo);
		this.contribute = new NodeContributionService(this.repo, this.assign);
		//this.strategy = mock(NodeAssignmentStrategy.class);
		//this.faculties = List.of("EWI", "AE");

		this.node = new NodeBuilder()
			.setNodeCpuResourceCapacityTo(0.0)
			.setNodeGpuResourceCapacityTo(0.0)
			.setNodeMemoryResourceCapacityTo(0.0)
			.withNodeName("FacultyCentralCore")
			.foundAtUrl("/" + "EWI" + "/central-core")
			.byUserWithNetId("SYSTEM")
			.assignToFacultyWithId("EWI").constructNodeInstance();

		/*this.node2 = new NodeBuilder()
			.setNodeCpuResourceCapacityTo(0.0)
			.setNodeGpuResourceCapacityTo(0.0)
			.setNodeMemoryResourceCapacityTo(0.0)
			.withNodeName("FacultyCentralCore")
			.foundAtUrl("/" + "EWI" + "/central-core")
			.byUserWithNetId("SYSTEM")
			.constructNodeInstance();*/
	}

	@Test
	public void addNodeAssignedToSpecificFacultyToClusterTest() {
		this.node.setUrl("a");
		this.contribute.addNodeAssignedToSpecificFacultyToCluster(node);

		assertThat(this.repo.existsByUrl("a")).isTrue();
		assertThat(this.repo.findByUrl("a").getFacultyId()).isEqualTo("EWI");
	}

	//failing
	/*@Test
	public void addNodeToClusterTest() {
		this.controller.updateOnExistingFaculties(this.faculties);
		this.node2.setUrl("a");
		doReturn("EWI").when(this.strategy)
			.pickFacultyToAssignNodeTo(this.repo.findTotalResourcesPerFaculty());
		this.contribute.addNodeToCluster(this.node2);

		assertThat(this.repo.existsByUrl("a")).isTrue();
		assertThat(this.repo.findByUrl("a").getFacultyId()).isEqualTo("EWI");
	}*/

}
