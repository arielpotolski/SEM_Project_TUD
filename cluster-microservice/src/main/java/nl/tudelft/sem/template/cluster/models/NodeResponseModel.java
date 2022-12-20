package nl.tudelft.sem.template.cluster.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
