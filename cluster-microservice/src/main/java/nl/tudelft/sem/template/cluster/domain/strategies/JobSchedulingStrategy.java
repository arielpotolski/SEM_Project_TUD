package nl.tudelft.sem.template.cluster.domain.strategies;

import nl.tudelft.sem.template.cluster.domain.cluster.AvailableResourcesForDate;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;

import java.time.LocalDate;
import java.util.List;

public interface JobSchedulingStrategy {

    LocalDate scheduleJobFor(List<AvailableResourcesForDate> availableResourcesForDates, Job job);

}
