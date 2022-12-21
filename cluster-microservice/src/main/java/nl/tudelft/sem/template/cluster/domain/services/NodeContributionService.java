package nl.tudelft.sem.template.cluster.domain.services;

import nl.tudelft.sem.template.cluster.domain.cluster.FacultyTotalResources;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.cluster.NodeRepository;
import nl.tudelft.sem.template.cluster.domain.providers.NumberProvider;
import nl.tudelft.sem.template.cluster.domain.strategies.AssignNodeToRandomFacultyStrategy;
import nl.tudelft.sem.template.cluster.domain.strategies.NodeAssignmentStrategy;
import nl.tudelft.sem.template.cluster.models.FacultyResourcesResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This class provides a service for contributing new nodes to the cluster. It uses the NodeAssignmentStrategy first to
 * give the node an assigned faculty, and then persists in the node repository.
 */
@Service
public class NodeContributionService {

    private transient NodeAssignmentStrategy strategy;

    private final transient NodeInformationAccessingService nodeInformationAccessingService;


    @Autowired
    public NodeContributionService(NodeInformationAccessingService nodeInformationService,
                                   NumberProvider numberProvider) {
        this.nodeInformationAccessingService = nodeInformationService;
        this.strategy = new AssignNodeToRandomFacultyStrategy(numberProvider);
    }

    /**
     * Change the strategy by which nodes are assigned to faculties.
     *
     * @param strategy the new strategy.
     */
    public void changeNodeAssignmentStrategy(NodeAssignmentStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Uses the current strategy to pick a faculty to which to assign the given node.
     *
     * @param node the node to assign to a faculty.
     */
    private void assignNodeToFaculty(Node node) {
        // pick faculty using strategy
        List<FacultyTotalResources> list = this.nodeInformationAccessingService.getAssignedResourcesPerFaculty();
        List<FacultyResourcesResponseModel> testableList = FacultyResourcesResponseModel
                .convertAllFacultyTotalResourcesToResponseModels(list);
        String chosenId = strategy.pickFacultyToAssignNodeTo(testableList);

        // set facultyId of node
        node.setFacultyId(chosenId);
    }

    /**
     * Adds to the repository a node that already is assigned to a faculty.
     *
     * @param node the node to be added.
     */
    public void addNodeAssignedToSpecificFacultyToCluster(Node node) {
        this.nodeInformationAccessingService.save(node);
    }

    /**
     * Assigns this node to a faculty and adds it to the cluster (persists in repository.)
     *
     * @param node the node to add to the cluster.
     */
    public void addNodeToCluster(Node node) {
        // node is unassigned - assign faculty
        this.assignNodeToFaculty(node);

        // persist to repository
        this.nodeInformationAccessingService.save(node);
    }


}
