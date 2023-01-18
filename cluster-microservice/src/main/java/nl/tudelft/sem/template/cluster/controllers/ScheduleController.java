package nl.tudelft.sem.template.cluster.controllers;

import java.util.List;
import nl.tudelft.sem.template.cluster.domain.builders.JobBuilder;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;
import nl.tudelft.sem.template.cluster.domain.events.NotificationEvent;
import nl.tudelft.sem.template.cluster.domain.providers.DateProvider;
import nl.tudelft.sem.template.cluster.domain.services.JobSchedulingService;
import nl.tudelft.sem.template.cluster.domain.services.SchedulingDataProcessingService;
import nl.tudelft.sem.template.cluster.models.JobRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class ScheduleController {

    private final transient JobSchedulingService scheduling;
    private final transient SchedulingDataProcessingService schedulingDataProcessingService;

    private final transient DateProvider dateProvider;

    private final transient ApplicationEventPublisher applicationEventPublisher;

    /**
     * Instantiates a new controller.
     */
    @Autowired
    public ScheduleController(JobSchedulingService scheduling, DateProvider dateProvider,
                              SchedulingDataProcessingService schedulingDataProcessingService,
                              ApplicationEventPublisher applicationEventPublisher) {
        this.scheduling = scheduling;
        this.dateProvider = dateProvider;
        this.schedulingDataProcessingService = schedulingDataProcessingService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Gets and returns all jobs in the schedule.
     *
     * @return list of all jobs in the schedule.
     */
    @GetMapping("/schedule")
    @PreAuthorize("hasRole('SYSADMIN')")
    public List<Job> getSchedule() {
        return this.schedulingDataProcessingService.getAllJobsFromSchedule();
    }

    /**
     * Accepts a request from the request service, converts it into a job and schedules it.
     *
     * @param jobModel the JobRequestModel which is the deserialized JSON of the request sent by the Request service.
     *
     * @return a ResponseEntity with an informative message.
     */
    @PostMapping("/request")
    public ResponseEntity<String> forwardRequestToCluster(@RequestBody JobRequestModel jobModel) {
        // extract job from request model
        Job job = new JobBuilder().requestedThroughFaculty(jobModel.getFacultyId())
                .requestedByUserWithNetId(jobModel.getUserNetId())
                .havingName(jobModel.getJobName())
                .withDescription(jobModel.getJobDescription())
                .needingCpuResources(jobModel.getRequiredCpu())
                .needingGpuResources(jobModel.getRequiredGpu())
                .needingMemoryResources(jobModel.getRequiredMemory())
                .preferredCompletedBeforeDate(jobModel.getPreferredCompletionDate())
                .constructJobInstance();

        // preferred completion date is in the future
        if (job.getPreferredCompletionDate().isBefore(this.dateProvider.getTomorrow())) {
            return ResponseEntity.badRequest()
                    .body("The requested job cannot require the cluster to compute it before "
                            + this.dateProvider.getTomorrow() + ".");
        }

        // the resources requested are cpu >= gpu and cpu >= memory
        if (!job.areResourcesNeededValid()) {
            return ResponseEntity.badRequest()
                    .body("The requested job cannot require more GPU or memory than CPU.");
        }

        // can job ever be scheduled
        if (!this.scheduling.checkIfJobCanBeScheduled(job)) {
            return ResponseEntity.badRequest()
                    .body("The requested job requires more resources than are assigned to the "
                            + job.getFacultyId() + " faculty.");
        }

        // schedule job
        var scheduledFor = this.scheduling.scheduleJob(job);
        applicationEventPublisher.publishEvent(
                new NotificationEvent(this, scheduledFor.toString(), "JOB",
                        "SCHEDULED", "Your job has been scheduled by the cluster!", job.getUserNetId()
                ));

        // return
        return ResponseEntity.ok("Successfully scheduled job.");
    }

}