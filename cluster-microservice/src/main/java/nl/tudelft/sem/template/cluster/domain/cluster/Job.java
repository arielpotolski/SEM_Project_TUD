package nl.tudelft.sem.template.cluster.domain.cluster;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "schedule")
public class Job {

    /**
     * Identifier for the job in the schedule.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, unique = true)
    private long id;

    @Column(name = "facultyId", nullable = false)
    private String facultyId;

    @Column(name = "userNetId", nullable = false)
    private final String userNetId;

    @Column(name = "jobName", nullable = false)
    private String jobName;

    @Column(name = "jobDescription", nullable = false)
    private String jobDescription;

    @Column(name = "requiredCPU", nullable = false)
    private double requiredCPU;

    @Column(name = "requiredGPU", nullable = false)
    private double requiredGPU;

    @Column(name = "requiredMemory", nullable = false)
    private double requiredMemory;

    @Column(name = "preferredCompletionDate", nullable = false)
//    @JsonSerialize()
//    @JsonDeserialize()
    private LocalDate preferredCompletionDate;

    @Column(name = "scheduledFor", nullable = false)
    private LocalDate scheduledFor;

    /**
     * Create a new Job (no-parameters constructor).
     */
    public Job() {
        this.userNetId = "placeholder";
    }

    /**
     * Create a new Job.
     *
     * @param facultyId the facultyId of the faculty through which the job has been requested.
     * @param userNetId the netId of the user who requested the job.
     * @param jobName the name of the job.
     * @param jobDescription the description of the job.
     * @param requiredCPU the cpu resources that this job requires.
     * @param requiredGPU the gpu resources that this job requires.
     * @param requiredMemory the memory resources that this job requires.
     * @param preferredCompletionDate the preferred date before which this job should be scheduled.
     */
    public Job(String facultyId, String userNetId, String jobName, String jobDescription,
               double requiredCPU, double requiredGPU, double requiredMemory, LocalDate preferredCompletionDate) {
        this.facultyId = facultyId;
        this.userNetId = userNetId;
        this.jobName = jobName;
        this.jobDescription = jobDescription;
        this.requiredCPU = requiredCPU;
        this.requiredGPU = requiredGPU;
        this.requiredMemory = requiredMemory;
        this.preferredCompletionDate = preferredCompletionDate;
    }

    /**
     * Gets and returns the facultyId of this job.
     *
     * @return the facultyId of this job.
     */
    public String getFacultyId() {
        return facultyId;
    }

    /**
     * Changes the facultyId of this job to the provided one.
     *
     * @param facultyId the facultyId to be assigned to this job.
     */
    public void changeFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }

    /**
     * Gets and returns the netId of the user who requested this job.
     *
     * @return the netId of the user who requested this job.
     */
    public String getUserNetId() {
        return userNetId;
    }

    /**
     * Gets and returns the name of this job.
     *
     * @return the name of this job.
     */
    public String getJobName() {
        return jobName;
    }

    /**
     * Changes the name of this job to the provided one.
     *
     * @param jobName the name to be assigned to this job.
     */
    public void changeJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * Gets and returns the description of this job.
     *
     * @return the description of this job.
     */
    public String getJobDescription() {
        return jobDescription;
    }

    /**
     * Changes the description of this job to the provided one.
     *
     * @param jobDescription the description to be assigned to this job.
     */
    public void changeJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    /**
     * Gets and returns the amount of CPU resources this job requires.
     *
     * @return the CPU resources this job requires.
     */
    public double getRequiredCPUResources() {
        return requiredCPU;
    }

    /**
     * Changes the amount of CPU resources that this job requires to the provided one.
     *
     * @param requiredCPU the required CPU resources amount to be assigned to this job.
     */
    public void changeRequiredCPUResources(double requiredCPU) {
        this.requiredCPU = requiredCPU;
    }

    /**
     * Gets and returns the amount of GPU resources this job requires.
     *
     * @return the GPU resources this job requires.
     */
    public double getRequiredGPUResources() {
        return requiredGPU;
    }

    /**
     * Changes the amount of GPU resources that this job requires to the provided one.
     *
     * @param requiredGPU the required GPU resources amount to be assigned to this job.
     */
    public void changeRequiredGPUResources(double requiredGPU) {
        this.requiredGPU = requiredGPU;
    }

    /**
     * Gets and returns the amount of memory resources this job requires.
     *
     * @return the memory resources this job requires.
     */
    public double getRequiredMemoryResources() {
        return requiredMemory;
    }

    /**
     * Changes the amount of memory resources that this job requires to the provided one.
     *
     * @param requiredMemory the required memory resources amount to be assigned to this job.
     */
    public void changeRequiredMemoryResources(double requiredMemory) {
        this.requiredMemory = requiredMemory;
    }

    /**
     * Gets and returns the preferred date before which this job should be completed.
     *
     * @return the preferred date before which this job should be completed.
     */
    public LocalDate getPreferredCompletionDate() {
        return preferredCompletionDate;
    }

    /**
     * Changes the preferred date before this job should be completed to the provided one.
     *
     * @param preferredCompletionDate the new date before which this job should be finished.
     */
    public void changePreferredCompletionDate(LocalDate preferredCompletionDate) {
        this.preferredCompletionDate = preferredCompletionDate;
    }

    /**
     * Gets and return the date that this job is currently scheduled for.
     *
     * @return the date this job is currently scheduled for.
     */
    public LocalDate getDateJobIsScheduledFor() {
        return scheduledFor;
    }

    /**
     * Changes the date this job is scheduled for to the provided one.
     *
     * @param scheduledFor the new scheduled date to be assigned to this job.
     */
    public void changeDateJobIsScheduledFor(LocalDate scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    /**
     * Compare this Job to another. Does not take scheduledFor into account, as if two equal jobs are scheduled
     * for two days independently, they are still nevertheless innately equal.
     *
     * @param o the other object to compare this job to.
     *
     * @return a boolean indicating whether this and o are equivalent jobs.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return Double.compare(job.requiredCPU, requiredCPU) == 0
                && Double.compare(job.requiredGPU, requiredGPU) == 0
                && Double.compare(job.requiredMemory, requiredMemory) == 0
                && Objects.equals(facultyId, job.facultyId)
                && Objects.equals(userNetId, job.userNetId)
                && Objects.equals(jobName, job.jobName)
                && Objects.equals(jobDescription, job.jobDescription)
                && Objects.equals(preferredCompletionDate, job.preferredCompletionDate);
    }

    /**
     * Hash this job.
     *
     * @return the integer hash code of this job instance.
     */
    @Override
    public int hashCode() {
        return Objects.hash(facultyId, userNetId, jobName, jobDescription,
                requiredCPU, requiredGPU, requiredMemory, preferredCompletionDate,
                scheduledFor);
    }
}
