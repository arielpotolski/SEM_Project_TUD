package nl.tudelft.sem.template.cluster.models;

import lombok.Data;

import java.time.LocalDate;

/**
 * Model representing a user request for a job.
 */
@Data
public class JobRequestModel {
    private String facultyId;
    private final String userNetId;
    private String jobName;
    private String jobDescription;
    private double requiredCPU;
    private double requiredGPU;
    private double requiredMemory;
    private LocalDate preferredCompletionDate;
}
