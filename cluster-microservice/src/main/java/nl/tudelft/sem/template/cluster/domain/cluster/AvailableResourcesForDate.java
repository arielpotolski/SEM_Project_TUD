package nl.tudelft.sem.template.cluster.domain.cluster;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableResourcesForDate {
    private LocalDate date;
    private double availableCpu;
    private double availableGpu;
    private double availableMemory;
}
