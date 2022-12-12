package nl.tudelft.sem.template.cluster.domain.builders;

import nl.tudelft.sem.template.cluster.domain.cluster.Node;

public class NodeBuilder implements NodeBuilderInterface {
    private double cpuResources;
    private double gpuResources;
    private double memoryResources;
    private String name;
    private String url;
    private String userNetId;
    private String facultyId = null;

    @Override
    public NodeBuilder setNodeCpuResourceCapacityTo(double cpuResources) {
        this.cpuResources = cpuResources;
        return this;
    }

    @Override
    public NodeBuilder setNodeGpuResourceCapacityTo(double gpuResources) {
        this.gpuResources = gpuResources;
        return this;
    }

    @Override
    public NodeBuilder setNodeMemoryResourceCapacityTo(double memoryResources) {
        this.memoryResources = memoryResources;
        return this;
    }

    @Override
    public NodeBuilder withNodeName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public NodeBuilder foundAtUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public NodeBuilder byUserWithNetId(String userNetId) {
        this.userNetId = userNetId;
        return this;
    }

    @Override
    public NodeBuilder assignToFacultyWithId(String facultyId) {
        this.facultyId = facultyId;
        return this;
    }

    @Override
    public Node constructNodeInstance() {
        var node = new Node(cpuResources, gpuResources, memoryResources, name, url, userNetId);
        node.setFacultyId(facultyId);
        return node;
    }

    public double getCpuResources() {
        return cpuResources;
    }

    public void setCpuResources(double cpuResources) {
        this.cpuResources = cpuResources;
    }

    public double getGpuResources() {
        return gpuResources;
    }

    public void setGpuResources(double gpuResources) {
        this.gpuResources = gpuResources;
    }

    public double getMemoryResources() {
        return memoryResources;
    }

    public void setMemoryResources(double memoryResources) {
        this.memoryResources = memoryResources;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUserNetId() {
        return userNetId;
    }

    public void setUserNetId(String userNetId) {
        this.userNetId = userNetId;
    }

    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }
}
