package nl.tudelft.sem.template.cluster.domain.strategies;

import nl.tudelft.sem.template.cluster.domain.cluster.AvailableResourcesForDate;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines a strategy in which a job is scheduled for the date with the most available resources in the preferred range.
 * If no match found, default to "earliest possible date" strategy starting with day after last preferred.
 */
public class LeastBusyDateStrategy {

    public LocalDate scheduleJobFor(List<AvailableResourcesForDate> availableResourcesForDates, Job job) {
        // index of date
        int index;
        if (job.getPreferredCompletionDate().isAfter(availableResourcesForDates
                .get(availableResourcesForDates.size() - 1).getDate()))
            index = availableResourcesForDates.size() - 1;
        else {
            index = availableResourcesForDates.stream()
                    .map(AvailableResourcesForDate::getDate).collect(Collectors.toList())
                    .indexOf(job.getPreferredCompletionDate());
        }

        var beforePreferredDate = availableResourcesForDates.subList(0, index + 1);
        beforePreferredDate.sort(Comparator.comparingDouble(x ->
                x.getAvailableCpu() + x.getAvailableGpu() + x.getAvailableMemory()));
        for (AvailableResourcesForDate availableResources : beforePreferredDate) {
            if (job.getRequiredCPUResources() <= availableResources.getAvailableCpu() &&
                    job.getRequiredGPUResources() <= availableResources.getAvailableGpu() &&
                    job.getRequiredMemoryResources() <= availableResources.getAvailableMemory())
                return availableResources.getDate();
        }

        // no match - default to the earliest possible
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
