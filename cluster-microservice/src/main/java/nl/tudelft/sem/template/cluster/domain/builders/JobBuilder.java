package nl.tudelft.sem.template.cluster.domain.builders;

import java.time.LocalDate;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;


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

    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }

    public String getUserNetId() {
        return userNetId;
    }

    public void setUserNetId(String userNetId) {
        this.userNetId = userNetId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getRequiredCpu() {
        return requiredCpu;
    }

    public void setRequiredCpu(double requiredCpu) {
        this.requiredCpu = requiredCpu;
    }

    public double getRequiredGpu() {
        return requiredGpu;
    }

    public void setRequiredGpu(double requiredGpu) {
        this.requiredGpu = requiredGpu;
    }

    public double getRequiredMemory() {
        return requiredMemory;
    }

    public void setRequiredMemory(double requiredMemory) {
        this.requiredMemory = requiredMemory;
    }

    public LocalDate getPreferredCompletionDate() {
        return preferredCompletionDate;
    }

    public void setPreferredCompletionDate(LocalDate preferredCompletionDate) {
        this.preferredCompletionDate = preferredCompletionDate;
    }
}
