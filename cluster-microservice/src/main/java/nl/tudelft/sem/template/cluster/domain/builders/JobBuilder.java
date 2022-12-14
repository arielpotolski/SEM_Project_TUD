package nl.tudelft.sem.template.cluster.domain.builders;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;

@Getter
@Setter
public class JobBuilder implements JobBuilderInterface {
    private String facultyId;
    private String userNetId;
    private String name;
    private String description;
    private double requiredCpu;
    private double requiredGpu;
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
        this.requiredCpu = cpuResources;
        return this;
    }

    @Override
    public JobBuilder needingGpuResources(double gpuResources) {
        this.requiredGpu = gpuResources;
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
                requiredCpu, requiredGpu, requiredMemory, preferredCompletionDate);
    }
}