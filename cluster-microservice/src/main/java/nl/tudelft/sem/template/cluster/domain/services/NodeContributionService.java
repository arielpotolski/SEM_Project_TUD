package nl.tudelft.sem.template.cluster.domain.services;

import java.util.ArrayList;
import java.util.List;
import lombok.Setter;
import nl.tudelft.sem.template.cluster.domain.cluster.FacultyTotalResources;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.events.NodesWereRemovedEvent;
import nl.tudelft.sem.template.cluster.domain.providers.NumberProvider;
import nl.tudelft.sem.template.cluster.domain.strategies.AssignNodeToRandomFacultyStrategy;
import nl.tudelft.sem.template.cluster.domain.strategies.NodeAssignmentStrategy;
import nl.tudelft.sem.template.cluster.models.FacultyResourcesResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
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

    private final transient NodeDataProcessingService nodeDataProcessingService;

    private final transient SchedulingDataProcessingService schedulingDataProcessingService;

    private List<Node> nodesToRemove;  // list of nodes to be removed after midnight.

    @Autowired
    private ApplicationEventPublisher publisher;

    /**
     * Instantiates a new NodeContributionService.
     *
     * @param schedulingDataProcessingService a dataProcessingService
     * @param numberProvider a numberProvider
     */
    @Autowired
    public NodeContributionService(NodeDataProcessingService nodeDataProcessingService,
                                   SchedulingDataProcessingService schedulingDataProcessingService,
                                   NumberProvider numberProvider) {
        this.nodeDataProcessingService = nodeDataProcessingService;
        this.schedulingDataProcessingService = schedulingDataProcessingService;
        this.strategy = new AssignNodeToRandomFacultyStrategy(numberProvider);
        this.nodesToRemove = new ArrayList<>();
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
        List<FacultyTotalResources> list = this.schedulingDataProcessingService.getAssignedResourcesPerFaculty();
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
        this.nodeDataProcessingService.save(node);
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
        this.nodeDataProcessingService.save(node);
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
        var removedNodes = new ArrayList<Node>();
        for (Node node : this.nodesToRemove) {
            Node n = this.nodeDataProcessingService.getByUrl(node.getUrl());
            removedNodes.add(n);
            this.nodeDataProcessingService.deleteNode(n);
        }

        // all at once
        publisher.publishEvent(new NodesWereRemovedEvent(this, removedNodes));

        this.nodesToRemove = new ArrayList<>();
    }

}
