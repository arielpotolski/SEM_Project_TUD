package nl.tudelft.sem.template.cluster.domain.services;

import java.util.List;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.cluster.NodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Provides access to node-related information from the repositories and processes it for further use.
 */
@Service
public class NodeDataProcessingService {

    private final transient NodeRepository nodeRepository;

    /**
     * Creates this service object.
     *
     * @param nodeRepository the node repository to get node information from.
     */
    @Autowired
    public NodeDataProcessingService(NodeRepository nodeRepository) {
        this.nodeRepository = nodeRepository;
    }

    public int getNumberOfNodesInRepository() {
        return (int) this.nodeRepository.count();
    }

    public boolean existsByUrl(String url) {
        return this.nodeRepository.existsByUrl(url);
    }

    public boolean existsByFacultyId(String facultyId) {
        return this.nodeRepository.existsByFacultyId(facultyId);
    }

    public Node getByUrl(String url) {
        return this.nodeRepository.findByUrl(url);
    }

    public List<Node> getByFacultyId(String facultyId) {
        return this.getAllNodes().stream().filter(x -> x.getFacultyId().equals(facultyId)).collect(Collectors.toList());
    }

    public List<Node> getAllNodes() {
        return this.nodeRepository.findAll();
    }

    public void save(Node node) {
        this.nodeRepository.save(node);
    }

    public void deleteNode(Node node) {
        this.nodeRepository.delete(node);
    }

    public void deleteAllNodes() {
        this.nodeRepository.deleteAll();
    }

}
