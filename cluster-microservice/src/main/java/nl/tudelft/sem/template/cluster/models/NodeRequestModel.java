package nl.tudelft.sem.template.cluster.models;

import lombok.Data;

@Data
public class NodeRequestModel {
    private double cpuResources;
    private double gpuResources;
    private double memoryResources;
    private String name;
    private String url;
}
