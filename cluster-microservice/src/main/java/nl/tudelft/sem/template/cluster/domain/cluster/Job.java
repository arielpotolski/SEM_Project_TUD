package nl.tudelft.sem.template.cluster.domain.cluster;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "schedule")
@Getter
@Setter
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

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Column(name = "preferredCompletionDate", nullable = false, columnDefinition = "DATE")
    private LocalDate preferredCompletionDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
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
     * This method calculates whether the amount of resources needed to execute the
     * job are valid. Namely, if the amount gpu and memory required is at most the
     * same as the amount of cpu required.
     *
     * @return true if valid, false if invalid.
     */
    public boolean areResourcesNeededValid() {
        if (this.requiredCpu < this.requiredGpu || this.requiredCpu < this.requiredMemory) {
            return false;
        }
        return true;
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
