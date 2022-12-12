package nl.tudelft.sem.template.cluster.domain.cluster;

import java.time.LocalDate;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
    private double requiredCpu;

    @Column(name = "requiredGPU", nullable = false)
    private double requiredGpu;

    @Column(name = "requiredMemory", nullable = false)
    private double requiredMemory;

    @Column(name = "preferredCompletionDate", nullable = false, columnDefinition = "DATE")
    private LocalDate preferredCompletionDate;

    @Column(name = "scheduledFor", nullable = false, columnDefinition = "DATE")
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
     * @param requiredCpu the cpu resources that this job requires.
     * @param requiredGpu the gpu resources that this job requires.
     * @param requiredMemory the memory resources that this job requires.
     * @param preferredCompletionDate the preferred date before which this job should be scheduled.
     */
    public Job(String facultyId, String userNetId, String jobName, String jobDescription,
               double requiredCpu, double requiredGpu, double requiredMemory, LocalDate preferredCompletionDate) {
        this.facultyId = facultyId;
        this.userNetId = userNetId;
        this.jobName = jobName;
        this.jobDescription = jobDescription;
        this.requiredCpu = requiredCpu;
        this.requiredGpu = requiredGpu;
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
    public void setFacultyId(String facultyId) {
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
    public void setJobName(String jobName) {
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
    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    /**
     * Gets and returns the amount of CPU resources this job requires.
     *
     * @return the CPU resources this job requires.
     */
    public double getRequiredCpu() {
        return requiredCpu;
    }

    /**
     * Changes the amount of CPU resources that this job requires to the provided one.
     *
     * @param requiredCpu the required CPU resources amount to be assigned to this job.
     */
    public void setRequiredCpu(double requiredCpu) {
        this.requiredCpu = requiredCpu;
    }

    /**
     * Gets and returns the amount of GPU resources this job requires.
     *
     * @return the GPU resources this job requires.
     */
    public double getRequiredGpu() {
        return requiredGpu;
    }

    /**
     * Changes the amount of GPU resources that this job requires to the provided one.
     *
     * @param requiredGpu the required GPU resources amount to be assigned to this job.
     */
    public void setRequiredGpu(double requiredGpu) {
        this.requiredGpu = requiredGpu;
    }

    /**
     * Gets and returns the amount of memory resources this job requires.
     *
     * @return the memory resources this job requires.
     */
    public double getRequiredMemory() {
        return requiredMemory;
    }

    /**
     * Changes the amount of memory resources that this job requires to the provided one.
     *
     * @param requiredMemory the required memory resources amount to be assigned to this job.
     */
    public void setRequiredMemory(double requiredMemory) {
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
    public void setPreferredCompletionDate(LocalDate preferredCompletionDate) {
        this.preferredCompletionDate = preferredCompletionDate;
    }

    /**
     * Gets and return the date that this job is currently scheduled for.
     *
     * @return the date this job is currently scheduled for.
     */
    public LocalDate getScheduledFor() {
        return scheduledFor;
    }

    /**
     * Changes the date this job is scheduled for to the provided one.
     *
     * @param scheduledFor the new scheduled date to be assigned to this job.
     */
    public void setScheduledFor(LocalDate scheduledFor) {
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Job job = (Job) o;
        return Double.compare(job.requiredCpu, requiredCpu) == 0
                && Double.compare(job.requiredGpu, requiredGpu) == 0
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
                requiredCpu, requiredGpu, requiredMemory, preferredCompletionDate,
                scheduledFor);
    }
}
