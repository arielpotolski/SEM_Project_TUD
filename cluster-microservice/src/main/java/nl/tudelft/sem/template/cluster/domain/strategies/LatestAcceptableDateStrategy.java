package nl.tudelft.sem.template.cluster.domain.strategies;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.cluster.domain.cluster.AvailableResourcesForDate;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;
import org.springframework.stereotype.Component;

/**
 * Defines a strategy in which a job is scheduled for the latest date that fits in the "preferred" boundaries.
 * Iteration begins at that date and goes back until the day after the current one. If no match is found, it
 * means that no possible date in the preferred range is available - defaults to "earliest possible date"
 * strategy.
 */
@Component
public class LatestAcceptableDateStrategy implements JobSchedulingStrategy {

    /**
     * Tries to find the latest date within the preferred date range to schedule the job on. If none are available,
     * finds the next earliest date after the preferred date.
     *
     * @param availableResourcesForDates the list of available resources for each considered date.
     * @param job the job to be scheduled.
     *
     * @return the date on which the job is to be scheduled.
     */
    public LocalDate scheduleJobFor(List<AvailableResourcesForDate> availableResourcesForDates, Job job) {
        // index of date
        int index = availableResourcesForDates.stream()
                    .map(AvailableResourcesForDate::getDate).collect(Collectors.toList())
                    .indexOf(job.getPreferredCompletionDate());

        // go backwards
        for (int i = index; i >= 0; --i) {
            var availableResources = availableResourcesForDates.get(i);
            if (job.getRequiredCpu() <= availableResources.getAvailableCpu()
                    && job.getRequiredGpu() <= availableResources.getAvailableGpu()
                    && job.getRequiredMemory() <= availableResources.getAvailableMemory()) {
                return availableResources.getDate();
            }
        }

        // no match - go forward from index
        for (int j = index; j < availableResourcesForDates.size(); ++j) {
            var availableResources = availableResourcesForDates.get(j);
            if (job.getRequiredCpu() <= availableResources.getAvailableCpu()
                    && job.getRequiredGpu() <= availableResources.getAvailableGpu()
                    && job.getRequiredMemory() <= availableResources.getAvailableMemory()) {
                return availableResources.getDate();
            }
        }

        // should never be reached
        return availableResourcesForDates.get(availableResourcesForDates.size() - 1).getDate();
    }

}
