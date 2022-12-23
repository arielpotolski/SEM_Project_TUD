package nl.tudelft.cse.sem.template.system.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NodeRequestModel {
    private double cpuResources;
    private double gpuResources;
    private double memoryResources;
    private String name;
    private String url;
}
