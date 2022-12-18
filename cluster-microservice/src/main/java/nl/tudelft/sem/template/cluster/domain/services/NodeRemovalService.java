package nl.tudelft.sem.template.cluster.domain.services;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.cluster.NodeRepository;
import nl.tudelft.sem.template.cluster.domain.providers.implementations.TimeToMidnightProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NodeRemovalService {

    private List<Node> nodesToRemove;  // list of nodes to be removed after midnight.
    private NodeRepository repo;
    private TimeToMidnightProvider timeProvider;

    /**
     * Constructor of the service class.
     *
     * @param repo initialize the service repository to this one
     */
    @Autowired
    public NodeRemovalService(NodeRepository repo) {
        this.nodesToRemove = new ArrayList<>();
        this.repo = repo;
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
    public int size() {
        return this.nodesToRemove.size();
    }

    /**
     * Gets the repository of this service.
     *
     * @return the repository of this service
     */
    public NodeRepository getRepo() {
        return this.repo;
    }

    /**
     * Adds a node to the list of nodes to be removed at midnight.
     *
     * @param node node to be added to the list
     */
    public void addNodeToBeRemoved(Node node) {
        this.nodesToRemove.add(node);
    }

    /**
     * This method removes the nodes in the nodesToRemoveList every time it's midnight.
     * The arguments in the schedule method are:
     * Remove: inner class that implements a runnable task (removing the nodes from the
     * 			list and from the nodeRepo);
     * timeProvider.timeToMidnight(): a method in a provider that calculates how much time
     * 			there is left to midnight (in Milliseconds);
     * TimeUnit.Milliseconds: defines the time unit to milliseconds.
     */
    public void removeNodesAtMidnight() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.schedule(new Remove(this), this.timeProvider.timeToMidnight(), TimeUnit.MILLISECONDS);
    }

    /**
     * This inner class implements the runnable argument required in the
     * schedule method. This class implements the removal of the nodes in
     * the list of nodesToRemove at midnight.
     */
    private class Remove implements Runnable {

        private NodeRemovalService service;

        /**
         * Constructor of the class. Initializes a new NodeRemovalService,
         * which will allow us to remove the nodes from the list nodesToRemove,
         * in the NodeRemovalService class.
         *
         * @param service the service to initialize the field of this class.
         */
        public Remove(NodeRemovalService service) {
            this.service = service;
        }

        @Override
        public void run() {
            removeNodes();
        }

        /**
         * Removes the nodes from the list nodesToRemove in the NodeRemovalService
         * class, as well as from the nodeRepository.
         */
        public void removeNodes() {
            List<Node> temp = this.service.getNodesToRemove();
            for (Node node : temp) {
                this.service.nodesToRemove.remove(node);
                this.service.repo.delete(node);
            }
        }
    }
}
