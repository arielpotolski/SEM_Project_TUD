package nl.tudelft.sem.template.cluster.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NodeResponseModel {
    private double cpuResources;
    private double gpuResources;
    private double memoryResources;
    private String name;
    private String url;
    private String userNetId;
    private String facultyId;

    /**
     * Converts the provided node into a response model.
     *
     * @param node the node to convert.
     *
     * @return the response model based on the provided node.
     */
    private static NodeResponseModel convertNodeToResponseModel(Node node) {
        return new NodeResponseModel(node.getCpuResources(), node.getGpuResources(), node.getMemoryResources(),
                node.getName(), node.getUrl(), node.getUserNetId(), node.getFacultyId());
    }

    /**
     * Converts all nodes in the provided list into NodeResponseModels.
     *
     * @param nodes the nodes to be converted into models.
     *
     * @return list of NodeResponseModels, each corresponding to one Node from the input list.
     */
    public static List<NodeResponseModel> convertAllNodesToResponseModels(List<Node> nodes) {
        List<NodeResponseModel> models = new ArrayList<>();
        for (Node node : nodes) {
            models.add(NodeResponseModel.convertNodeToResponseModel(node));
        }
        return models;
    }
}
