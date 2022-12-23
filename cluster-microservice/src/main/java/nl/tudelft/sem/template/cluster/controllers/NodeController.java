package nl.tudelft.sem.template.cluster.controllers;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import nl.tudelft.sem.template.cluster.authentication.AuthManager;
import nl.tudelft.sem.template.cluster.domain.builders.NodeBuilder;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.services.DataProcessingService;
import nl.tudelft.sem.template.cluster.domain.services.NodeContributionService;
import nl.tudelft.sem.template.cluster.models.NodeRequestModel;
import nl.tudelft.sem.template.cluster.models.NodeResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NodeController {

    private final transient AuthManager authManager;

    private final transient NodeContributionService nodeContributionService;
    private final transient DataProcessingService dataProcessingService;

    /**
     * Instantiates a new controller.
     *
     * @param authManager Spring Security component used to authenticate and authorize the user
     */
    @Autowired
    public NodeController(AuthManager authManager,
                             NodeContributionService nodeContributionService,
                             DataProcessingService dataProcessingService) {
        this.authManager = authManager;
        this.nodeContributionService = nodeContributionService;
        this.dataProcessingService = dataProcessingService;
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
            if (this.dataProcessingService.existsByFacultyId(faculty)) {
                continue;
            }
            Node core =  new NodeBuilder()
                    .setNodeCpuResourceCapacityTo(0.0)
                    .setNodeGpuResourceCapacityTo(0.0)
                    .setNodeMemoryResourceCapacityTo(0.0)
                    .withNodeName("FacultyCentralCore")
                    .foundAtUrl("/" + faculty + "/central-core")
                    .byUserWithNetId("SYSTEM")
                    .assignToFacultyWithId(faculty).constructNodeInstance();
            this.nodeContributionService.addNodeAssignedToSpecificFacultyToCluster(core);
        }
        return ResponseEntity.ok("Successfully acknowledged all existing faculties.");
    }

    /**
     * Provides an endpoint for accessing nodes directly. This can be either all nodes or a node at specified url.
     *
     * @param request the url to look for the node at (if given).
     *
     * @return response entity containing the list of all relevant nodes (or the looked for node when url provided and
     * exists in the database).
     */
    @GetMapping(value = {"/nodes", "/nodes/**"})
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<List<NodeResponseModel>> getNodeInformation(HttpServletRequest request) {
        String url = request.getRequestURI().replaceFirst("/nodes", "");
        String slashCheck = "/";
        if (url.isEmpty() || url.equals(slashCheck)) {
            var rawNodes = this.dataProcessingService.getAllNodes();
            return ResponseEntity.ok(NodeResponseModel.convertAllNodesToResponseModels(rawNodes));
        } else if (this.dataProcessingService.existsByUrl(url)) {
            return ResponseEntity.ok(NodeResponseModel
                .convertAllNodesToResponseModels(List.of(this.dataProcessingService.getByUrl(url))));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Adds a new node to the cluster. Fails if amount of cpu resources are not enough
     * or if there is already a node with the same url in the database.
     *
     * @param node the node to be added to the cluster
     * @return A string saying if the node was added. In case of failure, it returns a
     * 			string saying why is it failing
     */
    @PostMapping(path = {"/nodes/add", "/nodes/add/{facultyId}"})
    public ResponseEntity<String> addNode(@RequestBody NodeRequestModel node,
                                          @PathVariable(value = "facultyId", required = false) String facultyId) {
        if (this.dataProcessingService.getNumberOfNodesInRepository() == 0) {
            // Unknown faculties. All nodes will be assigned to the Board of Examiners until faculties become known.
            Node core =  new NodeBuilder()
                .setNodeCpuResourceCapacityTo(0.0)
                .setNodeGpuResourceCapacityTo(0.0)
                .setNodeMemoryResourceCapacityTo(0.0)
                .withNodeName("BoardCentralCore")
                .foundAtUrl("/board-of-examiners/central-core")
                .byUserWithNetId("SYSTEM")
                .assignToFacultyWithId("Board of Examiners").constructNodeInstance();
            this.nodeContributionService.addNodeAssignedToSpecificFacultyToCluster(core);
        }

        if (this.dataProcessingService.existsByUrl(node.getUrl())) {
            return ResponseEntity.badRequest().body("Failed to add node. A node with this url already exists.");
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

        // no preassigned faculty
        if (facultyId == null) {
            if (n.hasEnoughCpu().equals("Your node has been successfully added.")) {
                this.nodeContributionService.addNodeToCluster(n);
                return ResponseEntity.ok(n.hasEnoughCpu());
            }
            return ResponseEntity.badRequest().body(n.hasEnoughCpu());
        } else if (this.dataProcessingService.existsByFacultyId(facultyId)) {
            if (n.hasEnoughCpu().equals("Your node has been successfully added.")) {
                n.setFacultyId(facultyId);
                this.nodeContributionService.addNodeAssignedToSpecificFacultyToCluster(n);
                return ResponseEntity.ok(n.hasEnoughCpu());
            }
            return ResponseEntity.badRequest().body(n.hasEnoughCpu());
        } else {
            return ResponseEntity.badRequest().body("Unfortunately, this faculty does not exist in the cluster.");
        }
    }

    /**
     * This method deletes the node given by the url provided by the user. Since the
     * node can only be removed on the next day after the request, we do a post mapping
     * and add the node to be removed to a list stored in the NodeRemovalService. Once
     * we hit midnight, the nodes contained in that list will be removed. This method
     * will be the only removal method available to users that are not sysadmins.
     *
     * @param url the url of the node to be removed
     * @return a string saying whether the removal was successfully scheduled. If not,
     *          returns a string saying what went wrong
     */
    @PostMapping(value = "/nodes/delete/user/{url}")
    public ResponseEntity<String> scheduleNodeRemoval(@PathVariable("url") String url) {
        if (!this.nodeContributionService.getRepo().existsByUrl(url)) {
            return ResponseEntity.badRequest().body("Could not find the node to be deleted."
                + " Check if the url provided is correct.");
        } else if (!this.nodeContributionService.getRepo().findByUrl(url).getUserNetId()
            .equals(authManager.getNetId())) {
            return ResponseEntity.badRequest().body("You cannot remove nodes that"
                + " other users have contributed to the cluster.");
        }

        this.nodeContributionService
            .addNodeToBeRemoved(this.nodeContributionService.getRepo().findByUrl(url));

        return ResponseEntity.ok("Your node will be removed at midnight.");
    }

    /**
     * Delete all nodes or specified node, if url provided.
     *
     * @param request the url by which the node will be found and deleted, if provided.
     *
     * @return a string saying whether the node(s) was deleted or not
     */
    @DeleteMapping(value = {"/nodes/delete", "/nodes/delete/**"})
    @PreAuthorize("hasRole('SYSADMIN')")
    public ResponseEntity<String> deleteNode(HttpServletRequest request) {
        String url = request.getRequestURI().replaceFirst("/nodes/delete", "");
        String slashCheck = "/";
        if (url.isEmpty() || url.equals(slashCheck)) {
            this.dataProcessingService.deleteAllNodes();
            return ResponseEntity.ok("All nodes have been deleted from the cluster.");
        }

        if (this.dataProcessingService.existsByUrl(url)) {
            Node node = this.dataProcessingService.getByUrl(url);
            this.dataProcessingService.deleteNode(node);
            return ResponseEntity.ok("The node has been successfully deleted");
        } else {
            return ResponseEntity.ok("Could not find the node to be deleted."
                + " Check if the url provided is correct.");
        }
    }
}
