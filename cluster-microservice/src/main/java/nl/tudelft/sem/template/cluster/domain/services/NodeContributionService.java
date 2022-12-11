package nl.tudelft.sem.template.cluster.domain.services;

import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.cluster.NodeAssignmentService;
import nl.tudelft.sem.template.cluster.domain.cluster.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This class provides a service for contributing new nodes to the cluster. It uses the NodeAssignmentService first to
 * give the node an assigned faculty, and then persists in the node repository.
 */
@Service
public class NodeContributionService {

    private final transient NodeRepository repo;

    private final transient NodeAssignmentService assigning;

    @Autowired
    public NodeContributionService(NodeRepository repo, NodeAssignmentService assigning) {
        this.repo = repo;
        this.assigning = assigning;
    }

    public void addNodeAssignedToSpecificFacultyToCluster(Node node, String facultyId) {
        // assign to specified faculty
        node.changeNodeAssignedFacultyId(facultyId);

        // persist to repository
        this.repo.save(node);
    }

    public void addNodeToCluster(Node node) {
        // node is unassigned - assign faculty
        assigning.assignNodeToFaculty(node);

        // persist to repository
        this.repo.save(node);
    }


}