package nl.tudelft.sem.template.cluster.domain.cluster;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailableResourcesForDate {
    private LocalDate date;
    private double availableCpu;
    private double availableGpu;
    private double availableMemory;
}
