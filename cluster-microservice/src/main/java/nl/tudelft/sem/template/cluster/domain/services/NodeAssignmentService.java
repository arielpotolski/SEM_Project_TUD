package nl.tudelft.sem.template.cluster.domain.services;

import java.util.List;
import nl.tudelft.sem.template.cluster.domain.cluster.FacultyTotalResources;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.cluster.NodeRepository;
import nl.tudelft.sem.template.cluster.domain.providers.NumberProvider;
import nl.tudelft.sem.template.cluster.domain.strategies.AssignNodeToLeastResourcefulFacultyStrategy;
import nl.tudelft.sem.template.cluster.domain.strategies.AssignNodeToRandomFacultyStrategy;
import nl.tudelft.sem.template.cluster.domain.strategies.NodeAssignmentStrategy;
import nl.tudelft.sem.template.cluster.models.FacultyResourcesResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * This class provides a service which assigns a given node to a faculty based on the chosen strategy.
 */
@Service
public class NodeAssignmentService {

    private final transient NodeRepository repo;

    private transient NodeAssignmentStrategy strategy;

    private final transient NodeInformationAccessingService nodeInformationAccessingService;

    /**
     * Creates a new NodeAssignmentService object.
     *
     * @param repo the node repository to store all nodes.
     * @param numberProvider the random number provider for the default assignment strategy.
     * @param nodeInformationAccessingService service for accessing data from the repo
     */
    @Autowired
    public NodeAssignmentService(NodeRepository repo, NumberProvider numberProvider,
                                 NodeInformationAccessingService nodeInformationAccessingService) {
        this.repo = repo;
        this.strategy = new AssignNodeToRandomFacultyStrategy(numberProvider);
        this.nodeInformationAccessingService = nodeInformationAccessingService;
    }

    public void changeNodeAssignmentStrategy(NodeAssignmentStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Uses the current strategy to pick a faculty to which to assign the given node.
     *
     * @param node the node to assign to a faculty.
     */
    public void assignNodeToFaculty(Node node) {
        // pick faculty using strategy
        List<FacultyTotalResources> list = this.repo.findTotalResourcesPerFaculty();
        List<FacultyResourcesResponseModel> testableList = this.nodeInformationAccessingService
                .convertAllFacultyTotalResourcesToResponseModels(list);
        String chosenId = strategy.pickFacultyToAssignNodeTo(testableList);

        // set facultyId of node
        node.setFacultyId(chosenId);
    }

}
