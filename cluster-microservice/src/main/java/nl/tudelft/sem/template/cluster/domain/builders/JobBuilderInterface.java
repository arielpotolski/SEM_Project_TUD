package nl.tudelft.sem.template.cluster.domain.builders;

import nl.tudelft.sem.template.cluster.domain.cluster.Job;

import java.time.LocalDate;

public interface JobBuilderInterface {
    JobBuilderInterface requestedThroughFaculty(String facultyId);
    JobBuilderInterface requestedByUserWithNetId(String NetId);
    JobBuilderInterface havingName(String name);
    JobBuilderInterface withDescription(String description);
    JobBuilderInterface needingCpuResources(double cpuResources);
    JobBuilderInterface needingGpuResources(double gpuResources);
    JobBuilderInterface needingMemoryResources(double memoryResources);
    JobBuilderInterface preferredCompletedBeforeDate(LocalDate preferredCompletionDate);
    Job constructJobInstance();
}
