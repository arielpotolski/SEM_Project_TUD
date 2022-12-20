package nl.tudelft.sem.template.cluster.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatedResourcesResponseModel {
    private LocalDate date;
    private double totalCpu;
    private double totalGpu;
    private double totalMemory;
}
