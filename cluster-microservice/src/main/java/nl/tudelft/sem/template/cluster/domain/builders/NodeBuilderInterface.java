package nl.tudelft.sem.template.cluster.domain.builders;

import nl.tudelft.sem.template.cluster.domain.cluster.Node;

public interface NodeBuilderInterface {
    NodeBuilderInterface setCpuResources(double cpuResources);
    NodeBuilderInterface setGpuResources(double gpuResources);
    NodeBuilderInterface setMemoryResources(double memoryResources);
    NodeBuilderInterface setName(String name);
    NodeBuilderInterface setUrl(String url);
    NodeBuilderInterface setUserNetId(String userNetId);
    NodeBuilderInterface setFacultyId(String facultyId);
    Node build();
}
