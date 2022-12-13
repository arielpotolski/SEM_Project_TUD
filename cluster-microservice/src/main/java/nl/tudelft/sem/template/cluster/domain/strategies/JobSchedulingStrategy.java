package nl.tudelft.sem.template.cluster.domain.strategies;

import java.time.LocalDate;
import java.util.List;
import nl.tudelft.sem.template.cluster.domain.cluster.AvailableResourcesForDate;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;

public interface JobSchedulingStrategy {

    LocalDate scheduleJobFor(List<AvailableResourcesForDate> availableResourcesForDates, Job job);

}
