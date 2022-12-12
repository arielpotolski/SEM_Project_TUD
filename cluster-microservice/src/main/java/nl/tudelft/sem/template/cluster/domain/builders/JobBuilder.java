package nl.tudelft.sem.template.cluster.domain.builders;

import nl.tudelft.sem.template.cluster.domain.cluster.Job;

import java.time.LocalDate;

public class JobBuilder implements JobBuilderInterface {
    private String facultyId;
    private String userNetId;
    private String name;
    private String description;
    private double requiredCPU;
    private double requiredGPU;
    private double requiredMemory;
    private LocalDate preferredCompletionDate;

    @Override
    public JobBuilder requestedThroughFaculty(String facultyId) {
        this.facultyId = facultyId;
        return this;
    }

    @Override
    public JobBuilder requestedByUserWithNetId(String userNetId) {
        this.userNetId = userNetId;
        return this;
    }

    @Override
    public JobBuilder havingName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public JobBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public JobBuilder needingCpuResources(double cpuResources) {
        this.requiredCPU = cpuResources;
        return this;
    }

    @Override
    public JobBuilder needingGpuResources(double gpuResources) {
        this.requiredGPU = gpuResources;
        return this;
    }

    @Override
    public JobBuilder needingMemoryResources(double memoryResources) {
        this.requiredMemory = memoryResources;
        return this;
    }

    @Override
    public JobBuilder preferredCompletedBeforeDate(LocalDate preferredCompletionDate) {
        this.preferredCompletionDate = preferredCompletionDate;
        return this;
    }

    @Override
    public Job constructJobInstance() {
        return new Job(facultyId, userNetId, name, description,
                requiredCPU, requiredGPU, requiredMemory, preferredCompletionDate);
    }
}
