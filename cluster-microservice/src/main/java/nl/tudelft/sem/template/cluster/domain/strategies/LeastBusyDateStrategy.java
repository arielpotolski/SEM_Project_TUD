package nl.tudelft.sem.template.cluster.domain.strategies;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.cluster.domain.cluster.AvailableResourcesForDate;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;
import org.springframework.stereotype.Component;

/**
 * Defines a strategy in which a job is scheduled for the date with the most available resources in the preferred range.
 * If no match found, default to "earliest possible date" strategy starting with day after last preferred.
 */
@Component
public class LeastBusyDateStrategy implements JobSchedulingStrategy {

    /**
     * Finds the least busy date for the faculty through which the job was requested within the preferred date range
     * for which it is possible to schedule the job. If no such dates are available, returns earliest next date after
     * the preferred completion date of the job.
     *
     * @param availableResourcesForDates the list of resources available for each considered date.
     * @param job the job to be scheduled.
     *
     * @return the date on which the job is to be scheduled.
     */
    public LocalDate scheduleJobFor(List<AvailableResourcesForDate> availableResourcesForDates, Job job) {
        // index of date
        int index = availableResourcesForDates.stream()
                    .map(AvailableResourcesForDate::getDate).collect(Collectors.toList())
                    .indexOf(job.getPreferredCompletionDate());

        var beforePreferredDate = availableResourcesForDates.subList(0, index + 1);
        beforePreferredDate.sort(Comparator.comparingDouble((AvailableResourcesForDate x) ->
                x.getAvailableCpu() + x.getAvailableGpu() + x.getAvailableMemory()));
        for (AvailableResourcesForDate availableResources : beforePreferredDate) {
            if (job.getRequiredCpu() <= availableResources.getAvailableCpu()
                    && job.getRequiredGpu() <= availableResources.getAvailableGpu()
                    && job.getRequiredMemory() <= availableResources.getAvailableMemory()) {
                return availableResources.getDate();
            }
        }

        // no match - default to the earliest possible
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
