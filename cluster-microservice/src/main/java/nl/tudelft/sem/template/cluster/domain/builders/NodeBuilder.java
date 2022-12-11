package nl.tudelft.sem.template.cluster.domain.builders;

import nl.tudelft.sem.template.cluster.domain.cluster.Node;

public class NodeBuilder implements NodeBuilderInterface{
    private double cpuResources;
    private double gpuResources;
    private double memoryResources;
    private String name;
    private String url;
    private String userNetId;
    private String facultyId = null;

    @Override
    public NodeBuilder setCpuResources(double cpuResources) {
        this.cpuResources = cpuResources;
        return this;
    }

    @Override
    public NodeBuilder setGpuResources(double gpuResources) {
        this.gpuResources = gpuResources;
        return this;
    }

    @Override
    public NodeBuilder setMemoryResources(double memoryResources) {
        this.memoryResources = memoryResources;
        return this;
    }

    @Override
    public NodeBuilder setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public NodeBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public NodeBuilder setUserNetId(String userNetId) {
        this.userNetId = userNetId;
        return this;
    }

    @Override
    public NodeBuilder setFacultyId(String facultyId) {
        this.facultyId = facultyId;
        return this;
    }

    @Override
    public Node build() {
        var node = new Node(cpuResources, gpuResources, memoryResources, name, url, userNetId);
        node.changeNodeAssignedFacultyId(facultyId);
        return node;
    }
}
