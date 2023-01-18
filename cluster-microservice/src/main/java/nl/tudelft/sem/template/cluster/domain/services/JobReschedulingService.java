package nl.tudelft.sem.template.cluster.domain.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import nl.tudelft.sem.template.cluster.domain.cluster.AvailableResourcesForDate;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;
import nl.tudelft.sem.template.cluster.domain.events.NotificationEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Getter
@Service
public class JobReschedulingService {

    /**
     * Provides access to information related to the resources.
     */
    private final transient SchedulingDataProcessingService schedulingDataProcessingService;

    private final transient ApplicationEventPublisher publisher;

    private transient JobSchedulingService jobSchedulingService;


    /**
     * Constructor for JobReschedulingService that injects all needed services.
     *
     * @param schedulingDataProcessingService the service providing access to data.
     * @param applicationEventPublisher the event listerner for sending notifications
     * @param jobSchedulingService job scheduling service for scheduling new jobs
     */
    @Autowired
    public JobReschedulingService(SchedulingDataProcessingService schedulingDataProcessingService,
                                  ApplicationEventPublisher applicationEventPublisher,
                                  JobSchedulingService jobSchedulingService) {
        this.jobSchedulingService = jobSchedulingService;
        this.schedulingDataProcessingService = schedulingDataProcessingService;
        this.publisher = applicationEventPublisher;
    }




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
        List<Job> fullSchedule = this.schedulingDataProcessingService.getAllJobsFromSchedule();

        // available resources for each faculty
        for (String facultyId : faculties) {
            // this list will only contain dates with the resources, when at least one resource's balance is negative.
            // this means that some jobs need to be rescheduled
            var resourcesForDaysWhereReschedulingNecessary =
                    this.schedulingDataProcessingService
                            .getAvailableResourcesForGivenFacultyUntilDay(facultyId,
                                    this.schedulingDataProcessingService.findLatestDateWithReservedResources()).stream()
                            .filter(x -> x.getAvailableCpu() < 0
                                    || x.getAvailableGpu() < 0
                                    || x.getAvailableMemory() < 0)
                            .collect(Collectors.toList());

            // this faculty needs no rescheduling - all the jobs are still within available resources
            if (!resourcesForDaysWhereReschedulingNecessary.isEmpty()) {
                // go over all days with insufficient resources
                jobsToReschedule.addAll(removeScheduledJobs(
                        fullSchedule, resourcesForDaysWhereReschedulingNecessary, facultyId));
            }
        }
        rescheduleJobs(jobsToReschedule);



    }

    /**
     * This is a helper function for rescheduling jobs where nodes were removed,
     * it removes jobs until there are enough resources.
     * And gives back a list of the removed jobs.
     *
     * @param fullSchedule a schedule of all the jobs that are already scheduled
     * @param resourcesForDaysWhereReschedulingNecessary A list of the resources for the certain days
     * @param facultyId the faculty at hand
     * @return List of jobs that need to be rescheduled
     */
    public List<Job> removeScheduledJobs(List<Job> fullSchedule, List<AvailableResourcesForDate>
            resourcesForDaysWhereReschedulingNecessary, String facultyId) {
        List<Job> jobsToReschedule = new ArrayList<>();
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
                this.schedulingDataProcessingService.deleteJob(removedJob);

                // append to temp to reschedule
                jobsToReschedule.add(removedJob);

                // update resources
                cpu += removedJob.getRequiredCpu();
                gpu += removedJob.getRequiredGpu();
                memory += removedJob.getRequiredMemory();
            }
        }
        return jobsToReschedule;
    }

    /** This method is a helper function for rescheduling jobs if nodes are removed.
     * It also sends a notification on the job status.
     *
     * @param jobsToReschedule A list of jobs to schedule agian.
     */
    public void rescheduleJobs(List<Job> jobsToReschedule) {

        for (Job jobToReschedule : jobsToReschedule) {
            // check if job can ever be scheduled, drop if no
            if (!jobSchedulingService.checkIfJobCanBeScheduled(jobToReschedule)) {
                //send notification of dropping
                publisher.publishEvent(
                        new NotificationEvent(this, jobToReschedule.getScheduledFor().toString(), "JOB",
                                "DROPPED", "Your job has been dropped by the cluster!"
                                + " We are sorry for the inconvenience.", jobToReschedule.getUserNetId()
                        ));
                continue;
            }

            // reschedule if yes
            jobSchedulingService.scheduleJob(jobToReschedule);
            publisher.publishEvent(
                    new NotificationEvent(this, jobToReschedule.getScheduledFor().toString(), "JOB",
                            "RESCHEDULED", "Your job has been rescheduled by the cluster!",
                            jobToReschedule.getUserNetId()
                    ));
        }
    }
}
