package nl.tudelft.sem.template.cluster.domain.services;

import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.cluster.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableAsync
@EnableScheduling
public class NodeRemovalService {

    private List<Node> nodesToRemove;  // list of nodes to be removed after midnight.

    private NodeRepository nodeRepo;

    /**
     * Constructor of the service class.
     *
     * @param repo initialize the service repository to this one
     */
    @Autowired
    public NodeRemovalService(NodeRepository repo) {
        this.nodesToRemove = new ArrayList<>();
        this.nodeRepo = repo;
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
        return this.nodeRepo;
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
    @Scheduled(cron = "0 33 4 * * *")
    @Async
    public void removeNodesAtMidnight() {
        System.out.println(this.nodeRepo.count());
        for (Node node : this.nodesToRemove) {
            Node n = this.nodeRepo.findByUrl(node.getUrl());
            this.nodeRepo.delete(n);
        }
        System.out.println(this.nodeRepo.count());
        this.nodesToRemove = new ArrayList<>();
    }
}
