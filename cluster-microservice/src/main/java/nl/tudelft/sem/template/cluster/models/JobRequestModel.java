package nl.tudelft.sem.template.cluster.models;

import java.time.LocalDate;
import lombok.Data;

/**
 * Model representing a user request for a job.
 */
@Data
public class JobRequestModel {
    private String facultyId;
    private String userNetId;
    private String jobName;
    private String jobDescription;
    private double requiredCpu;
    private double requiredGpu;
    private double requiredMemory;
    private LocalDate preferredCompletionDate;
}
