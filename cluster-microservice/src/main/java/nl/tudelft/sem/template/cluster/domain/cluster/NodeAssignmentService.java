package nl.tudelft.sem.template.cluster.domain.cluster;

import java.util.List;
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
        this.strategy = new AssignNodeToLeastResourcefulFacultyStrategy();
    }

    public void changeNodeAssignmentStrategy(NodeAssignmentStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Uses the current startegy to pick a faculty to which to assign the given node.
     *
     * @param node the node to assign to a faculty.
     */
    public void assignNodeToFaculty(Node node) {
        // pick faculty using strategy
        List<FacultyTotalResources> list = this.repo.findTotalResourcesPerFaculty();
        String chosenId = strategy.pickFacultyToAssignNodeTo(list);

        // set facultyId of node
        node.changeNodeAssignedFacultyId(chosenId);
    }

}
