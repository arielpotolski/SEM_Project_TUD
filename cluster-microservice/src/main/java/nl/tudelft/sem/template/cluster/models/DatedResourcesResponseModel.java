package nl.tudelft.sem.template.cluster.models;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatedResourcesResponseModel {
    private LocalDate date;
    private double totalCpu;
    private double totalGpu;
    private double totalMemory;
}
