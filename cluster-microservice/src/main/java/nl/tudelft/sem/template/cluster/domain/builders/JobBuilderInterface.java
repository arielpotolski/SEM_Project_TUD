package nl.tudelft.sem.template.cluster.domain.builders;

import java.time.LocalDate;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;

public interface JobBuilderInterface {
    JobBuilderInterface requestedThroughFaculty(String facultyId);

    JobBuilderInterface requestedByUserWithNetId(String netId);

    JobBuilderInterface havingName(String name);

    JobBuilderInterface withDescription(String description);

    JobBuilderInterface needingCpuResources(double cpuResources);

    JobBuilderInterface needingGpuResources(double gpuResources);

    JobBuilderInterface needingMemoryResources(double memoryResources);

    JobBuilderInterface preferredCompletedBeforeDate(LocalDate preferredCompletionDate);

    Job constructJobInstance();
}
