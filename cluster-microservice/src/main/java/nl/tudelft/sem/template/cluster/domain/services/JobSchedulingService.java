package nl.tudelft.sem.template.cluster.domain.services;

import java.time.LocalDate;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;
import nl.tudelft.sem.template.cluster.domain.cluster.JobScheduleRepository;
import nl.tudelft.sem.template.cluster.domain.cluster.NodeRepository;
import nl.tudelft.sem.template.cluster.domain.providers.DateProvider;
import nl.tudelft.sem.template.cluster.domain.strategies.JobSchedulingStrategy;
import nl.tudelft.sem.template.cluster.domain.strategies.LeastBusyDateStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service handles scheduling a job according to the current strategy.
 */
@Service
public class JobSchedulingService {

    /**
     * Provides access to information related to the resources.
     */
    private final transient DataProcessingService resourceInfo;

    /**
     * Current strategy of scheduling jobs.
     */
    private transient JobSchedulingStrategy strategy;

    /**
     * Creates a new JobSchedulingService object and injects the repository.
     *
     * @param resourceInfo the service providing access to data.
     */
    @Autowired
    public JobSchedulingService(DataProcessingService resourceInfo) {
        this.resourceInfo = resourceInfo;

        // default strategy: first come, first served; the earliest possible date
        this.strategy = new LeastBusyDateStrategy();
    }

    /**
     * Changes the scheduling strategy to the provided one.
     *
     * @param strategy the scheduling strategy by which this service will be scheduling jobs.
     */
    public void changeSchedulingStrategy(JobSchedulingStrategy strategy) {
        this.strategy = strategy;
    }



    /**
     * Checks whether the job's requested resources are all smaller than the total available for the faculty through
     * which the request was made.
     *
     * @param job the job to check against the available resources.
     *
     * @return boolean indicating whether it is possible, within the foreseeable future, to schedule the job
     */
    public boolean checkIfJobCanBeScheduled(Job job) {
        if (!this.resourceInfo.existsByFacultyId(job.getFacultyId())) {
            return false;
        }
        var assignedResources = this.resourceInfo.getAssignedResourcesForGivenFaculty(job.getFacultyId());
        return !(job.getRequiredCpu() > assignedResources.getCpu_Resources())
                && !(job.getRequiredGpu() > assignedResources.getGpu_Resources())
                && !(job.getRequiredMemory() > assignedResources.getMemory_Resources());
    }

    /**
     * Uses the current scheduling strategy to schedule the given job. Persists the scheduled job in the repository.
     *
     * @param job the job to be scheduled.
     */
    public void scheduleJob(Job job) {
        // available resources from tomorrow to day after last scheduled job, inclusive
        // this way, since a check whether this job can be scheduled has been passed, the job can always be fit into
        // the schedule
        var maxDateInSchedule = this.resourceInfo.findLatestDateWithReservedResources();
        if (job.getPreferredCompletionDate().isAfter(maxDateInSchedule)) {
            maxDateInSchedule = job.getPreferredCompletionDate();
        }
        var availableResourcesPerDay = this.resourceInfo
                .getAvailableResourcesForGivenFacultyUntilDay(job.getFacultyId(),
                        maxDateInSchedule.plusDays(1));

        // use strategy to determine a date to schedule the job for
        var dateToScheduleJob = this.strategy.scheduleJobFor(availableResourcesPerDay, job);

        // assign scheduled date to job
        job.setScheduledFor(dateToScheduleJob);

        // save to schedule
        this.resourceInfo.saveInSchedule(job);
    }

}
