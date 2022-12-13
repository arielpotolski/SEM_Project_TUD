package nl.tudelft.sem.template.cluster.domain.builders;

import lombok.Getter;
import lombok.Setter;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;

@Getter
@Setter
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
}
