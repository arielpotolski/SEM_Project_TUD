package nl.tudelft.sem.template.cluster.domain.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.cluster.domain.cluster.FacultyTotalResources;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.cluster.NodeRepository;
import nl.tudelft.sem.template.cluster.models.FacultyResourcesResponseModel;
import nl.tudelft.sem.template.cluster.models.NodeResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NodeInformationAccessingService {

    @SuppressWarnings("PMD.BeanMembersShouldSerialize")
    private final NodeRepository nodeRepository;

    @Autowired
    public NodeInformationAccessingService(NodeRepository nodeRepository) {
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

    public NodeResponseModel convertNodeToResponseModel(Node node) {
        return new NodeResponseModel(node.getCpuResources(), node.getGpuResources(), node.getMemoryResources(),
                node.getName(), node.getUrl(), node.getUserNetId(), node.getFacultyId());
    }

    public List<NodeResponseModel> convertAllNodesToResponseModels(List<Node> nodes) {
        List<NodeResponseModel> models = new ArrayList<>();
        for (Node node : nodes) {
            models.add(this.convertNodeToResponseModel(node));
        }
        return models;
    }

    public FacultyResourcesResponseModel convertFacultyTotalResourcesToResponseModel(FacultyTotalResources rawResources) {
        return new FacultyResourcesResponseModel(rawResources.getFaculty_Id(), rawResources.getCpu_Resources(),
                rawResources.getGpu_Resources(), rawResources.getMemory_Resources());
    }

    public List<FacultyResourcesResponseModel> convertAllFacultyTotalResourcesToResponseModels
            (List<FacultyTotalResources> rawResources) {
        List<FacultyResourcesResponseModel> models = new ArrayList<>();
        for (FacultyTotalResources resources : rawResources) {
            models.add(this.convertFacultyTotalResourcesToResponseModel(resources));
        }
        return models;
    }

    public List<Node> getAllNodes() {
        return this.nodeRepository.findAll();
    }

    public Node getByUrl(String url) {
        return this.nodeRepository.findByUrl(url);
    }

    public List<Node> getByFacultyId(String facultyId) {
        return this.getAllNodes().stream().filter(x -> x.getFacultyId().equals(facultyId)).collect(Collectors.toList());
    }

    public void deleteNode(Node node) {
        this.nodeRepository.delete(node);
    }

    public void deleteAllNodes() {
        this.nodeRepository.deleteAll();
    }

    public List<FacultyTotalResources> getAssignedResourcesPerFaculty() {
        return this.nodeRepository.findTotalResourcesPerFaculty();
    }

    /**
     * Gets and returns the total resources in the three categories assigned to the specified faculty.
     *
     * @param facultyId the facultyId for which to find assigned resources.
     *
     * @return the total assigned resources for this facultyId, as well as the facultyId.
     */
    public FacultyTotalResources getAssignedResourcesForGivenFaculty(String facultyId) {
        return getAssignedResourcesPerFaculty().stream()
                .filter(x -> x.getFaculty_Id().equals(facultyId))
                .collect(Collectors.toList()).get(0);
    }

}
