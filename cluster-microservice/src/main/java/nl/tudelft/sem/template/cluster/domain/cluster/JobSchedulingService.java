package nl.tudelft.sem.template.cluster.domain.cluster;

import nl.tudelft.sem.template.cluster.domain.cluster.Job;
import nl.tudelft.sem.template.cluster.domain.cluster.JobScheduleRepository;
import nl.tudelft.sem.template.cluster.domain.strategies.EarliestPossibleDateStrategy;
import nl.tudelft.sem.template.cluster.domain.strategies.JobSchedulingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * This service handles scheduling a job according to the current strategy.
 */
@Service
public class JobSchedulingService {

    /**
     * The schedule repository to save scheduled jobs into.
     */
    private final transient JobScheduleRepository jobScheduleRepo;

    /**
     * The node repository containing all the cluster's nodes.
     */
    private final transient NodeRepository nodeRepo;

    /**
     * Provides access to information related to the resources.
     */
    private final transient ResourceInformationAccessingService resourceInfo;


    /**
     * Current strategy of scheduling jobs.
     */
    private transient JobSchedulingStrategy strategy;

    /**
     * Creates a new JobSchedulingService object and injects the repository.
     *
     * @param jobScheduleRepo the JobScheduleRepository that jobs are scheduled in.
     * @param nodeRepo the NodeRepository that contains all the nodes of the cluster.
     */
    @Autowired
    public JobSchedulingService(JobScheduleRepository jobScheduleRepo, NodeRepository nodeRepo,
                                ResourceInformationAccessingService resourceInfo) {
        this.jobScheduleRepo = jobScheduleRepo;
        this.nodeRepo = nodeRepo;
        this.resourceInfo = resourceInfo;

        // default strategy: first come, first served; the earliest possible date
        this.strategy = new EarliestPossibleDateStrategy();
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
     * Queries the schedule for the latest date that occurs.
     *
     * @return the latest date in the schedule.
     */
    public LocalDate findLatestDateWithReservedResources() {
        return this.jobScheduleRepo.findMaximumDate();
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
        var assignedResources = nodeRepo.findTotalResourcesForGivenFaculty(job.getFacultyId());
        return !(job.getRequiredCPUResources() > assignedResources.getCpu_Resources()) &&
                !(job.getRequiredGPUResources() > assignedResources.getGpu_Resources()) &&
                !(job.getRequiredMemoryResources() > assignedResources.getMemory_Resources());
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
        var availableResourcesPerDay = resourceInfo
                .getAvailableResourcesForGivenFacultyUntilDay(job.getFacultyId(),
                        this.findLatestDateWithReservedResources().plusDays(2));

        // use strategy to determine a date to schedule the job for
        var dateToScheduleJob = strategy.scheduleJobFor(availableResourcesPerDay, job);

        // assign scheduled date to job
        job.changeDateJobIsScheduledFor(dateToScheduleJob);

        // save to schedule
        jobScheduleRepo.save(job);
    }

}
