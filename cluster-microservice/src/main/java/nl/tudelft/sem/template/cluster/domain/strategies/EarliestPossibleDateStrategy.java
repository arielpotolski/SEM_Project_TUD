package nl.tudelft.sem.template.cluster.domain.strategies;

import java.time.LocalDate;
import java.util.List;
import nl.tudelft.sem.template.cluster.domain.cluster.AvailableResourcesForDate;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;

/**
 * Defines a strategy in which a job is scheduled for the earliest available date.
 */
public class EarliestPossibleDateStrategy implements JobSchedulingStrategy {

    /**
     * Looks for the earliest date in the list, on which the provided job can be scheduled.
     *
     * @param availableResourcesForDates the list of available resources for each considered day.
     * @param job the job to schedule.
     *
     * @return the date on which the job is to be scheduled.
     */
    public LocalDate scheduleJobFor(List<AvailableResourcesForDate> availableResourcesForDates, Job job) {
        for (AvailableResourcesForDate availableResources : availableResourcesForDates) {
            if (job.getRequiredCpu() <= availableResources.getAvailableCpu()
                    && job.getRequiredGpu() <= availableResources.getAvailableGpu()
                    && job.getRequiredMemory() <= availableResources.getAvailableMemory()) {
                return availableResources.getDate();
            }
        }

        // this should never be reached because a check is run first if there is the job can ever be scheduled
        // only such jobs are passed to this method, and as the last scheduled job is in the penultimate position,
        // the last date will have all resources free
        return availableResourcesForDates.get(availableResourcesForDates.size() - 1).getDate();
    }

}
