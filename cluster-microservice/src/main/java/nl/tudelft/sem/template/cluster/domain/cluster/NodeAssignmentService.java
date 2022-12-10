package nl.tudelft.sem.template.cluster.domain.cluster;

import nl.tudelft.sem.template.cluster.domain.strategies.AssignNodeToLeastResourcefulFacultyStrategy;
import nl.tudelft.sem.template.cluster.domain.strategies.AssignNodeToRandomFacultyStrategy;
import nl.tudelft.sem.template.cluster.domain.strategies.NodeAssignmentStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This class provides a service which assigns a given node to a faculty based on the chosen strategy.
 */
@Service
public class NodeAssignmentService {

    private final transient NodeRepository repo;
    private transient NodeAssignmentStrategy strategy;

    @Autowired
    public NodeAssignmentService(NodeRepository repo) {
        this.repo = repo;
        this.strategy = new AssignNodeToRandomFacultyStrategy();
    }

    public void setStrategy(NodeAssignmentStrategy strategy) {
        this.strategy = strategy;
    }

    public void assignNodeToFaculty(Node node) {
        // pick faculty using strategy
        String chosenId = strategy.pickFacultyToAssignNodeTo(this.repo.findTotalResourcesPerFaculty());

        // set facultyId of node
        node.changeNodeAssignedFacultyId(chosenId);
    }

}
