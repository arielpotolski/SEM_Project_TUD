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

     public static JobRequestRequestModel convertToRequestModel(Request request) {
         return new JobRequestRequestModel(request.getFaculty(), request.getNetId(),
                 request.getName(), request.getDescription(),
                 request.getCpu(), request.getGpu(), request.getMemory(),
                 request.getPreferredDate());
     }
}
