package nl.tudelft.sem.template.cluster.domain.builders;

import nl.tudelft.sem.template.cluster.domain.cluster.Node;

public interface NodeBuilderInterface {
    NodeBuilderInterface setNodeCpuResourceCapacityTo(double cpuResources);

    NodeBuilderInterface setNodeGpuResourceCapacityTo(double gpuResources);

    NodeBuilderInterface setNodeMemoryResourceCapacityTo(double memoryResources);

    NodeBuilderInterface withNodeName(String name);

    NodeBuilderInterface foundAtUrl(String url);

    NodeBuilderInterface byUserWithNetId(String userNetId);

    NodeBuilderInterface assignToFacultyWithId(String facultyId);

    Node constructNodeInstance();
}
