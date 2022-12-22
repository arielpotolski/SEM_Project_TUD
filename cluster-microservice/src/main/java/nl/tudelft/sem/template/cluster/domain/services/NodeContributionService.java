package nl.tudelft.sem.template.cluster.domain.services;

import java.util.ArrayList;
import java.util.List;
import lombok.Setter;
import nl.tudelft.sem.template.cluster.domain.cluster.FacultyTotalResources;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.cluster.NodeRepository;
import nl.tudelft.sem.template.cluster.domain.providers.NumberProvider;
import nl.tudelft.sem.template.cluster.domain.strategies.AssignNodeToRandomFacultyStrategy;
import nl.tudelft.sem.template.cluster.domain.strategies.NodeAssignmentStrategy;
import nl.tudelft.sem.template.cluster.models.FacultyResourcesResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * This class provides a service for contributing new nodes to the cluster. It uses the NodeAssignmentStrategy first to
 * give the node an assigned faculty, and then persists in the node repository.
 */
@Service
@EnableAsync
@EnableScheduling
@Setter
public class NodeContributionService {

    private transient NodeAssignmentStrategy strategy;

    private final transient DataProcessingService dataProcessingService;

    private List<Node> nodesToRemove;  // list of nodes to be removed after midnight.

    private final transient NodeRepository nodeRepository;


    /**
     * Instantiates a new NodeContributionService.
     * 
     * @param dataProcessingService a dataProcessingService
     * @param numberProvider a numberProvider
     * @param nodeRepository a nodeRepository
     */
    @Autowired
    public NodeContributionService(DataProcessingService dataProcessingService,
                                   NumberProvider numberProvider, NodeRepository nodeRepository) {
        this.dataProcessingService = dataProcessingService;
        this.strategy = new AssignNodeToRandomFacultyStrategy(numberProvider);
        this.nodesToRemove = new ArrayList<>();
        this.nodeRepository = nodeRepository;
    }

    /**
     * Gets the list of nodes to be removed.
     *
     * @return list of nodes to be removed
     */
    public List<Node> getNodesToRemove() {
        return this.nodesToRemove;
    }

    /**
     * The number of nodes to be removed at midnight.
     *
     * @return the number of nodes to be removed
     */
    public int numberOfNodesToRemove() {
        return this.nodesToRemove.size();
    }

    /**
     * Gets the repository of this service.
     *
     * @return the repository of this service
     */
    public NodeRepository getRepo() {
        return this.nodeRepository;
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
        List<FacultyTotalResources> list = this.dataProcessingService.getAssignedResourcesPerFaculty();
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
        this.dataProcessingService.save(node);
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
        this.dataProcessingService.save(node);
    }

    /**
     * Adds a node to the list of nodes to be removed at midnight.
     *
     * @param node node to be added to the list
     */
    public boolean addNodeToBeRemoved(Node node) {
        this.nodesToRemove.add(node);
        return true;
    }

    /**
     * This method removes the nodes in the nodesToRemoveList every time it's midnight.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Async
    public void removeNodesAtMidnight() {
        for (Node node : this.nodesToRemove) {
            Node n = this.nodeRepository.findByUrl(node.getUrl());
            this.nodeRepository.delete(n);
        }

        this.nodesToRemove = new ArrayList<>();
    }

}
