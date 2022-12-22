package nl.tudelft.sem.template.example.domain;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobRequestRequestModel {
    private String facultyId;
    private String userNetId;
    private String jobName;
    private String jobDescription;
    private double requiredCpu;
    private double requiredGpu;
    private double requiredMemory;
    private LocalDate preferredCompletionDate;

    /**
     * Converts this request to a request model so that it can be read by the cluster.
     *
     * @param request the request to be converted.
     *
     * @return the request model to be sent.
     */
    public static JobRequestRequestModel convertToRequestModel(Request request) {
        return new JobRequestRequestModel(request.getFaculty(), request.getNetId(),
                request.getName(), request.getDescription(),
                request.getCpu(), request.getGpu(), request.getMemory(),
                request.getPreferredDate());
    }
}
