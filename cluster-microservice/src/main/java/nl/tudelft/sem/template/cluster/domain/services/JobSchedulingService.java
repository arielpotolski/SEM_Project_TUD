package nl.tudelft.sem.template.cluster.domain.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import nl.tudelft.sem.template.cluster.domain.cluster.AvailableResourcesForDate;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;
import nl.tudelft.sem.template.cluster.domain.events.NotificationEvent;
import nl.tudelft.sem.template.cluster.domain.providers.DateProvider;
import nl.tudelft.sem.template.cluster.domain.strategies.JobSchedulingStrategy;
import nl.tudelft.sem.template.cluster.domain.strategies.LeastBusyDateStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * This service handles scheduling a job according to the current strategy.
 */
@Getter
@Service
@EnableAsync
@EnableScheduling
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class JobSchedulingService {

    /**
     * Provides access to information related to the resources.
     */
    private final transient DataProcessingService dataProcessingService;

    /**
     * Current strategy of scheduling jobs.
     */
    private transient JobSchedulingStrategy strategy;

    /**
     * The provider for dates.
     */
    private final transient DateProvider dateProvider;

    private final transient ApplicationEventPublisher publisher;

    /**
     * Creates a new JobSchedulingService object and injects the repository.
     *
     * @param dataProcessingService the service providing access to data.
     */
    @Autowired
    public JobSchedulingService(DataProcessingService dataProcessingService, DateProvider dateProvider,
                                ApplicationEventPublisher publisher) {
        this.dateProvider = dateProvider;
        this.publisher = publisher;
        this.dataProcessingService = dataProcessingService;

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
     * This method sends notifications every midnight: to all users whose jobs are in the schedule for the previous day,
     * it sends a FINISHED notification, while those that are scheduled for today get a STARTED notification.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Async
    public void sendNotificationsOfStartedAndCompletedJobs() {
        // get all jobs for yesterday
        var jobsCompleted = this.dataProcessingService.getAllJobsFromSchedule().stream()
                .filter(x -> x.getScheduledFor().isEqual(dateProvider.getCurrentDate().minusDays(1)))
                .collect(Collectors.toList());

        // send notifications of completion
        for (Job completed : jobsCompleted) {
            publisher.publishEvent(new NotificationEvent(
                    this, dateProvider.getCurrentDate().minusDays(1).toString(),
                    "JOB", "COMPLETED", "The job you requested has been completed!",
                    completed.getUserNetId()
            ));
        }

        // get all jobs for today
        var jobsStarted = this.dataProcessingService.getAllJobsFromSchedule().stream()
                .filter(x -> x.getScheduledFor().isEqual(dateProvider.getCurrentDate()))
                .collect(Collectors.toList());

        // send notifications of commencement
        for (Job started : jobsStarted) {
            publisher.publishEvent(new NotificationEvent(
                    this, dateProvider.getCurrentDate().toString(),
                    "JOB", "STARTED", "The job you requested has been started!",
                    started.getUserNetId()
            ));
        }
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
        if (!this.dataProcessingService.existsByFacultyId(job.getFacultyId())) {
            return false;
        }
        var assignedResources = this.dataProcessingService.getAssignedResourcesForGivenFaculty(job.getFacultyId());
        return !(job.getRequiredCpu() > assignedResources.getCpu_Resources())
                && !(job.getRequiredGpu() > assignedResources.getGpu_Resources())
                && !(job.getRequiredMemory() > assignedResources.getMemory_Resources());
    }

    /**
     * Uses the current scheduling strategy to schedule the given job. Persists the scheduled job in the repository.
     *
     * @param job the job to be scheduled.
     *
     * @return the date the job is scheduled for.
     */
    public LocalDate scheduleJob(Job job) {
        // available resources from tomorrow to day after last scheduled job, inclusive
        // this way, since a check whether this job can be scheduled has been passed, the job can always be fit into
        // the schedule
        var maxDateInSchedule = this.dataProcessingService.findLatestDateWithReservedResources();
        if (job.getPreferredCompletionDate().isAfter(maxDateInSchedule)) {
            maxDateInSchedule = job.getPreferredCompletionDate();
        }
        var availableResourcesPerDay = this.dataProcessingService
                .getAvailableResourcesForGivenFacultyUntilDay(job.getFacultyId(),
                        maxDateInSchedule.plusDays(1));

        // use strategy to determine a date to schedule the job for
        var dateToScheduleJob = this.strategy.scheduleJobFor(availableResourcesPerDay, job);

        // assign scheduled date to job
        job.setScheduledFor(dateToScheduleJob);

        // save to schedule
        this.dataProcessingService.saveInSchedule(job);

        return dateToScheduleJob;
    }

    // FOR RESCHEDULING

    /**
     * This is triggered by the NodesWereRemovedEvent. The listener extracts all facultyIds from the removed nodes and
     * sends the unique values to this method. This method then goes over each of the faculties, checking whether the
     * available resources for any day have gone below zero, which indicates that rescheduling is necessary. Then,
     * for each such day, jobs are removed and put in a temporary list. Once available resources are non-negative again,
     * the method moves on to other days and then faculties. At the end of these loops, there should no longer be days
     * when more resources are reserved than available.
     * After that, the method iterates over temp, and for each job
     * checks whether it can ever be scheduled (i.e., if its resource requirement does not exceed its faculty's new
     * total assigned resources after removal of some nodes.) If it cannot, it is dropped and a notification sent to the
     * user who requested the job. If it can be, scheduleJob is called to insert the job back into the schedule. A delay
     * notification is sent to the user along with the new scheduled date.
     *
     * @param faculties all the faculties who have lost nodes in the removal that triggered the event.
     */
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public void rescheduleJobsForFacultiesWithRemovedNodes(List<String> faculties) {
        // temp
        List<Job> jobsToReschedule = new ArrayList<>();

        // accessing repository only once to optimize
        List<Job> fullSchedule = this.dataProcessingService.getAllJobsFromSchedule();

        // available resources for each faculty
        for (String facultyId : faculties) {
            // this list will only contain dates with the resources, when at least one resource's balance is negative.
            // this means that some jobs need to be rescheduled
            var resourcesForDaysWhereReschedulingNecessary =
                    this.dataProcessingService
                            .getAvailableResourcesForGivenFacultyUntilDay(facultyId,
                                    this.dataProcessingService.findLatestDateWithReservedResources()).stream()
                            .filter(x -> x.getAvailableCpu() < 0
                                    || x.getAvailableGpu() < 0
                                    || x.getAvailableMemory() < 0)
                            .collect(Collectors.toList());

            // this faculty needs no rescheduling - all the jobs are still within available resources
            if (resourcesForDaysWhereReschedulingNecessary.isEmpty()) {
                continue;
            }

            // go over all days with insufficient resources
            for (AvailableResourcesForDate lackingResources : resourcesForDaysWhereReschedulingNecessary) {
                // get jobs scheduled for that date and faculty sorted descendingly by total cost
                var jobsOnProblematicDay = fullSchedule.stream()
                        .filter(x -> x.getFacultyId().equals(facultyId))
                        .filter(x -> x.getScheduledFor().equals(lackingResources.getDate()))
                        .sorted(Comparator.comparingDouble(
                                (Job x) -> x.getRequiredCpu() + x.getRequiredGpu() + x.getRequiredMemory()
                        ).reversed())
                        .collect(Collectors.toList());

                // remove until all available values are non-negative
                double cpu = lackingResources.getAvailableCpu();
                double gpu = lackingResources.getAvailableGpu();
                double memory = lackingResources.getAvailableMemory();
                while (!jobsOnProblematicDay.isEmpty() && (cpu < 0 || gpu < 0 || memory < 0)) {
                    var removedJob = jobsOnProblematicDay.remove(0); // the most costly job
                    this.dataProcessingService.deleteJob(removedJob);

                    // append to temp to reschedule
                    jobsToReschedule.add(removedJob);

                    // update resources
                    cpu += removedJob.getRequiredCpu();
                    gpu += removedJob.getRequiredGpu();
                    memory += removedJob.getRequiredMemory();
                }
            }
        }

        // go through temp
        for (Job jobToReschedule : jobsToReschedule) {
            // check if job can ever be scheduled, drop if no
            if (!this.checkIfJobCanBeScheduled(jobToReschedule)) {
                //send notification of dropping
                publisher.publishEvent(
                    new NotificationEvent(this, null, "JOB",
                        "DROPPED", "Your job has been dropped by the cluster!"
                        + " We are sorry for the inconvenience.", jobToReschedule.getUserNetId()
                    ));
                continue;
            }

            // reschedule if yes
            // TODO: send notification of rescheduling
            this.scheduleJob(jobToReschedule);
            publisher.publishEvent(
                new NotificationEvent(this, jobToReschedule.getScheduledFor().toString(), "JOB",
                    "SCHEDULED", "Your job has been rescheduled by the cluster!",
                    jobToReschedule.getUserNetId()
                ));
        }
    }

}
