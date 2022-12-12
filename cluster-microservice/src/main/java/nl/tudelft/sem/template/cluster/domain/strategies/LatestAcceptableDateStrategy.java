package nl.tudelft.sem.template.cluster.domain.strategies;

import nl.tudelft.sem.template.cluster.domain.cluster.AvailableResourcesForDate;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines a strategy in which a job is scheduled for the latest date that fits in the "preferred" boundaries.
 * Iteration begins at that date and goes back until the day after the current one. If no match is found, it
 * means that no possible date in the preferred range is available - defaults to "earliest possible date"
 * strategy.
 */
public class LatestAcceptableDateStrategy implements JobSchedulingStrategy{

    public LocalDate scheduleJobFor(List<AvailableResourcesForDate> availableResourcesForDates, Job job) {
        // index of date
        int index = availableResourcesForDates.stream()
                    .map(AvailableResourcesForDate::getDate).collect(Collectors.toList())
                    .indexOf(job.getPreferredCompletionDate());

        // go backwards
        for (int i = index; i >= 0; --i) {
            var availableResources = availableResourcesForDates.get(i);
            if (job.getRequiredCPUResources() <= availableResources.getAvailableCpu() &&
                    job.getRequiredGPUResources() <= availableResources.getAvailableGpu() &&
                    job.getRequiredMemoryResources() <= availableResources.getAvailableMemory())
                return availableResources.getDate();
        }

        // no match - go forward from index
        for (int j = index; j < availableResourcesForDates.size(); ++j) {
            var availableResources = availableResourcesForDates.get(j);
            if (job.getRequiredCPUResources() <= availableResources.getAvailableCpu() &&
                    job.getRequiredGPUResources() <= availableResources.getAvailableGpu() &&
                    job.getRequiredMemoryResources() <= availableResources.getAvailableMemory())
                return availableResources.getDate();
        }

        // should never be reached
        return availableResourcesForDates.get(availableResourcesForDates.size() - 1).getDate();
    }

}
