package nl.tudelft.sem.template.cluster.models;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacultyDatedResourcesResponseModel {
    private LocalDate date;
    private String facultyId;
    private double totalCpu;
    private double totalGpu;
    private double totalMemory;
}
