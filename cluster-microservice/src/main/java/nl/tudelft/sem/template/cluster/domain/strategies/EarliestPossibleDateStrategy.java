package nl.tudelft.sem.template.cluster.domain.strategies;

import nl.tudelft.sem.template.cluster.domain.cluster.AvailableResourcesForDate;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;

import java.time.LocalDate;
import java.util.List;

/**
 * Defines a strategy in which a job is scheduled for the earliest available date.
 */
public class EarliestPossibleDateStrategy implements JobSchedulingStrategy{

    public LocalDate scheduleJobFor(List<AvailableResourcesForDate> availableResourcesForDates, Job job) {
        for (AvailableResourcesForDate availableResources : availableResourcesForDates) {
            if (job.getRequiredCPUResources() <= availableResources.getAvailableCpu() &&
                job.getRequiredGPUResources() <= availableResources.getAvailableGpu() &&
                job.getRequiredMemoryResources() <= availableResources.getAvailableMemory())
                return availableResources.getDate();
        }
        // this should never be reached because a check is run first if there is the job can ever be scheduled
        // only such jobs are passed to this method, and as the last scheduled job is in the penultimate position,
        // the last date will have all resources free
        return availableResourcesForDates.get(availableResourcesForDates.size() - 1).getDate();
    }

}
