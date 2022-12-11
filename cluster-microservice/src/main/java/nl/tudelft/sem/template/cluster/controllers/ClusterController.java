package nl.tudelft.sem.template.cluster.controllers;

import nl.tudelft.sem.template.cluster.authentication.AuthManager;
import nl.tudelft.sem.template.cluster.domain.builders.NodeBuilder;
import nl.tudelft.sem.template.cluster.domain.cluster.*;
import nl.tudelft.sem.template.cluster.domain.services.NodeContributionService;
import nl.tudelft.sem.template.cluster.models.JobRequestModel;
import nl.tudelft.sem.template.cluster.models.NodeRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ClusterController {

	private final transient AuthManager authManager;

	private final transient NodeRepository nodeRep;
	private final transient JobScheduleRepository jobScheduleRep;

	private final transient JobSchedulingService scheduling;
	private final transient NodeContributionService contribution;

	/**
	 * Instantiates a new controller.
	 *
	 * @param authManager Spring Security component used to authenticate and authorize the user
	 */
	@Autowired
	public ClusterController(AuthManager authManager, NodeRepository nodeRep,
							 JobScheduleRepository jobScheduleRep, JobSchedulingService scheduling,
							 NodeContributionService contribution) {
		this.authManager = authManager;
		this.nodeRep = nodeRep;
		this.jobScheduleRep = jobScheduleRep;
		this.scheduling = scheduling;
		this.contribution = contribution;
	}

	/**
	 * Gets example by id.
	 *
	 * @return the example found in the database with the given id
	 */
	@GetMapping("/hello")
	public ResponseEntity<String> helloWorld() {
		return ResponseEntity.ok("Hello " + authManager.getNetId());
	}

	/**
	 * Gets all nodes stored in the cluster.
	 *
	 * @return a list of containing all the nodes
	 */
	@GetMapping("/all")
	public List<Node> getAll() {
		return this.nodeRep.findAll();
	}

	/**
	 * Gets a node by the url.
	 *
	 * @param url the url of the specific node to be found
	 * @return the node that has this url. It does not return anything if the url is
	 * 			not found in the cluster
	 */
	@GetMapping("/{url}")
	public ResponseEntity<Node> getNode(@PathVariable("url") String url) {
		return ResponseEntity.of(this.nodeRep.findByUrl(url));
	}

	/**
	 * Sets up the "central cores" of the existing faculties. They are nodes with 0 of each type of resource which
	 * serve as placeholders to ensure that the cluster knows which faculties exist and can, for example, be assigned
	 * nodes. This endpoint should be used by the User service as soon as both it and Cluster are online.
	 *
	 * @param faculties the list of faculties that exist in the User service's database.
	 *
	 * @return message of confirmation that the faculties have been received.
	 */
	@PostMapping("/faculties")
	public ResponseEntity<String> updateOnExistingFaculties(@RequestBody List<String> faculties) {
		for (String faculty : faculties) {
			if (jobScheduleRep.existsByFacultyId(faculty)) continue;
			Node core =  new NodeBuilder()
								.setNodeCpuResourceCapacityTo(0.0)
								.setNodeGpuResourceCapacityTo(0.0)
								.setNodeMemoryResourceCapacityTo(0.0)
								.withNodeName("FacultyCentralCore")
								.foundAtUrl("/" + faculty + "/central-core")
								.byUserWithNetId("SYSTEM")
								.assignToFacultyWithId(faculty).constructNodeInstance();
			contribution.addNodeAssignedToSpecificFacultyToCluster(core);
		}
		return ResponseEntity.ok("Successfully acknowledged all existing faculties.");
	}

//	@PostMapping("/request")
//	public ResponseEntity<String> forwardRequestToCluster(@RequestBody JobRequestModel jobModel) {
//
//	}

	/**
	 * Adds a new node to the cluster. Fails if amount of cpu resources are not enough
	 * or if there is already a node with the same url in the database.
	 *
	 * @param node the node to be added to the cluster
	 * @return A string saying if the node was added. In case of failure, it returns a
	 * 			string saying why is it failing
	 */
	@PostMapping(path = {"/add"})
	public ResponseEntity<String>  addNode(@RequestBody NodeRequestModel node) {
		// Check if central cores have been installed by User Service
		if ((int) this.nodeRep.count() == 0) {
			// Central cores not installed - faculties unknown. All nodes will be assigned to the
			// Board of Examiners until faculties become known.
			Node core =  new NodeBuilder()
					.setNodeCpuResourceCapacityTo(0.0)
					.setNodeGpuResourceCapacityTo(0.0)
					.setNodeMemoryResourceCapacityTo(0.0)
					.withNodeName("BoardCentralCore")
					.foundAtUrl("/board-pf-examiners/central-core")
					.byUserWithNetId("SYSTEM")
					.assignToFacultyWithId("Board of Examiners").constructNodeInstance();
			contribution.addNodeAssignedToSpecificFacultyToCluster(core);
		}


		if (this.nodeRep.existsByUrl(node.getUrl())) {
			return ResponseEntity.ok("Failed to add node. A node with this url already exists.");
		}
		String netId = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		Node n = new NodeBuilder()
				.setNodeCpuResourceCapacityTo(node.getCpuResources())
				.setNodeGpuResourceCapacityTo(node.getGpuResources())
				.setNodeMemoryResourceCapacityTo(node.getMemoryResources())
				.withNodeName(node.getName())
				.foundAtUrl(node.getUrl())
				.byUserWithNetId(netId)
				.constructNodeInstance();

		if (n.hasEnoughCPU().equals("Your node has been successfully added.")) {
			contribution.addNodeToCluster(n);
		}
		return ResponseEntity.ok(n.hasEnoughCPU());
	}

	/**
	 * Delete a node from the cluster by url. It does nothing in case the url
	 * does not exist.
	 *
	 * @param url the url by which the node will be found and deleted
	 * @return a string saying whether the node was deleted or not
	 */
	@DeleteMapping("/{url}")
	public ResponseEntity<String> deleteNode(@PathVariable("url") String url) {
		if (nodeRep.existsByUrl(url)) {
			Node node = nodeRep.findByUrl(url).get();
			this.nodeRep.delete(node);
			return ResponseEntity.ok("The node has been successfully deleted");
		} else {
			return ResponseEntity.ok("Could not find the node to be deleted." +
				" Check if the url provided is correct.");
		}
	}

	/**
	 * TODO: change this to post when we have a queue
	 *
	 * Deletes all the nodes from the cluster.
	 *
	 * @return a string saying if the deletion was successful
	 */
	@DeleteMapping("/all")
	public ResponseEntity<String> removeAll() {
		this.nodeRep.deleteAll();
		return ResponseEntity.ok("All nodes have been deleted from the cluster.");
	}

	/**
	 * Returns all faculties which have any node assigned to them and the total sum of resources in each of the three
	 * categories (CPU, GPU, memory) that they control.
	 *
	 * @return a list of Spring Projection Interfaces containing the facultyId and the sum of available CPU, GPU, and
	 * memory resources for that faculty.
	 */
	@GetMapping("/resources/all")
	public List<FacultyTotalResources> getResourcesByFaculty() {
		return this.nodeRep.findTotalResourcesPerFaculty();
	}

	/**
	 * Returns the sum of the resources of each of the three categories (CPU, GPU, memory) that the given facultyId
	 * has assigned.
	 *
	 * @param facultyId the facultyId for which to return the assigned resources.
	 *
	 * @return a Spring Projection Interface containing the facultyId and the sum of available CPU, GPU, and
	 * memory resources for that faculty.
	 */
	@GetMapping("/resources/all/{facultyId}")
	public FacultyTotalResources getResourcesForGivenFaculty(@PathVariable("facultyId") String facultyId) {
		return this.nodeRep.findTotalResourcesForGivenFaculty(facultyId);
	}



	// free resources per day

	// free resources per day for given faculty

}
