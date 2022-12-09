package nl.tudelft.sem.template.cluster.controllers;

import nl.tudelft.sem.template.cluster.authentication.AuthManager;
import nl.tudelft.sem.template.cluster.domain.cluster.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
	private final transient NodeAssignmentService assigning;

	/**
	 * Instantiates a new controller.
	 *
	 * @param authManager Spring Security component used to authenticate and authorize the user
	 */
	@Autowired
	public ClusterController(AuthManager authManager, NodeRepository nodeRep,
							 JobScheduleRepository jobScheduleRep, JobSchedulingService scheduling,
							 NodeAssignmentService assigning) {
		this.authManager = authManager;
		this.nodeRep = nodeRep;
		this.jobScheduleRep = jobScheduleRep;
		this.scheduling = scheduling;
		this.assigning = assigning;
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
	 * Adds a new node to the cluster. Fails if amount of cpu resources are not enough
	 * or if there is already a node with the same url in the database.
	 *
	 * @param node the node to be added to the cluster
	 * @return A string saying if the node was added. In case of failure, it returns a
	 * 			string saying why is it failing
	 */
	@PostMapping(path = {"/add"})
	public ResponseEntity<String>  addNode(@RequestBody Node node) {
		if (this.nodeRep.existsByUrl(node.getUrlOfNode())) {
			return ResponseEntity.ok("Failed to add node. A node with this url already exists.");
		}
		if (node.hasEnoughCPU().equals("Your node has been successfully added.")) {
			this.nodeRep.save(node);
		}
		return ResponseEntity.ok(node.hasEnoughCPU());
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

	@GetMapping("/resources")
	public List<FacultyTotalResources> getResourcesByFaculty() {
		return this.nodeRep.findTotalResourcesPerFaculty();
	}

	@GetMapping("/resources/{facultyId}")
	public FacultyTotalResources getResourcesForGivenFaculty(@PathVariable("facultyId") String facultyId) {
		return this.nodeRep.findTotalResourcesForGivenFaculty(facultyId);
	}

	// free resources per day

	// free resources per day for given faculty

}
