package nl.tudelft.sem.template.cluster.controllers;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import nl.tudelft.sem.template.cluster.domain.builders.NodeBuilder;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.services.NodeContributionService;
import nl.tudelft.sem.template.cluster.domain.services.NodeDataProcessingService;
import nl.tudelft.sem.template.cluster.models.NodeResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * This restController class will contain all endpoints that are related to the accessing of existing nodes.
 */
@RestController
public class NodeAccessController {

    private final transient NodeContributionService nodeContributionService;
    private final transient NodeDataProcessingService dataProcessingService;


    /**
     * Instantiates a new controller.*
     */
    @Autowired
    public NodeAccessController(NodeContributionService nodeContributionService,
                          NodeDataProcessingService dataProcessingService) {
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
}
