package nl.tudelft.sem.template.cluster.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacultyResourcesResponseModel {
    private String facultyId;
    private double totalCpu;
    private double totalGpu;
    private double totalMemory;
}
