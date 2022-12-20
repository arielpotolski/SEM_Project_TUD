package nl.tudelft.sem.template.cluster.services;

import nl.tudelft.sem.template.cluster.controllers.ClusterController;
import nl.tudelft.sem.template.cluster.domain.builders.NodeBuilder;
import nl.tudelft.sem.template.cluster.domain.cluster.FacultyTotalResources;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.cluster.NodeRepository;
import nl.tudelft.sem.template.cluster.domain.services.NodeAssignmentService;
import nl.tudelft.sem.template.cluster.domain.strategies.AssignNodeToLeastResourcefulFacultyStrategy;
import nl.tudelft.sem.template.cluster.domain.strategies.AssignNodeToRandomFacultyStrategy;
import nl.tudelft.sem.template.cluster.domain.strategies.NodeAssignmentStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
public class NodeAssignmentServiceTest {

	@Autowired
	private transient NodeRepository repo;

	@Autowired
	private transient NodeAssignmentService service;

	@Autowired
	private transient ClusterController controller;

	private List faculties;

	private transient NodeAssignmentStrategy mockedStrategy;

	private Node node;

	@BeforeEach
	void setUp() {
		this.service = new NodeAssignmentService(this.repo);
		this.faculties = List.of("EWI", "AE");
		this.mockedStrategy = mock(NodeAssignmentStrategy.class);

		this.node = new NodeBuilder()
			.setNodeCpuResourceCapacityTo(0.0)
			.setNodeGpuResourceCapacityTo(0.0)
			.setNodeMemoryResourceCapacityTo(0.0)
			.withNodeName("FacultyCentralCore")
			.foundAtUrl("/" + "EWI" + "/central-core")
			.byUserWithNetId("SYSTEM")
			.constructNodeInstance();
	}

	@Test
	public void changeStrategyTest() {
		assertThat(this.service.getStrategy())
			.isInstanceOf(AssignNodeToLeastResourcefulFacultyStrategy.class);

		NodeAssignmentStrategy s = new AssignNodeToRandomFacultyStrategy();
		this.service.changeNodeAssignmentStrategy(s);

		assertThat(this.service.getStrategy())
			.isInstanceOf(AssignNodeToRandomFacultyStrategy.class);
	}

	// Not properly working
	@Test
	public void assignNodeToFacultyAETest() {
		this.controller.updateOnExistingFaculties(this.faculties);
		when(this.mockedStrategy.pickFacultyToAssignNodeTo(this.repo.findTotalResourcesPerFaculty()))
			.thenReturn("AE");
		this.service.assignNodeToFaculty(this.node);

		assertThat(this.node.getFacultyId()).isEqualTo("AE");
	}

	/*@Test
	public void assignNodeToFacultyEWITest() {
		this.controller.updateOnExistingFaculties(this.faculties);
		when(this.mockedStrategy.pickFacultyToAssignNodeTo(this.repo.findTotalResourcesPerFaculty()))
			.thenReturn("EWI");
		this.service.assignNodeToFaculty(this.node);

		assertThat(this.node.getFacultyId()).isEqualTo("EWI");
	}*/

}
