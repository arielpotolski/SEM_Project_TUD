package nl.tudelft.sem.template.cluster.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import nl.tudelft.sem.template.cluster.domain.providers.DateProvider;
import nl.tudelft.sem.template.cluster.domain.services.JobSchedulingService;
import nl.tudelft.sem.template.cluster.domain.services.NodeContributionService;
import nl.tudelft.sem.template.cluster.domain.services.NodeDataProcessingService;
import nl.tudelft.sem.template.cluster.domain.services.PrivilegeVerificationService;
import nl.tudelft.sem.template.cluster.domain.services.SchedulingDataProcessingService;
import nl.tudelft.sem.template.cluster.models.FacultyDatedResourcesResponseModel;
import nl.tudelft.sem.template.cluster.models.FacultyResourcesResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class FacultyResourcesInformationController {

    private final transient JobSchedulingService scheduling;
    private final transient NodeDataProcessingService nodeDataProcessingService;
    private final transient SchedulingDataProcessingService schedulingDataProcessingService;
    private final transient PrivilegeVerificationService privilegeVerificationService;

    private final transient DateProvider dateProvider;

    private final transient ApplicationEventPublisher applicationEventPublisher;

    /**
     * Instantiates a new controller.
     */
    @Autowired
    public FacultyResourcesInformationController(JobSchedulingService scheduling,
                              NodeContributionService nodeContributionService, DateProvider dateProvider,
                              NodeDataProcessingService nodeDataProcessingService,
                              SchedulingDataProcessingService schedulingDataProcessingService,
                              PrivilegeVerificationService privilegeVerificationService,
                              ApplicationEventPublisher applicationEventPublisher) {
        this.scheduling = scheduling;
        this.nodeDataProcessingService = nodeDataProcessingService;
        this.dateProvider = dateProvider;
        this.schedulingDataProcessingService = schedulingDataProcessingService;
        this.privilegeVerificationService = privilegeVerificationService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Gets and returns the total amount of resources in each of the three categories (CPU, GPU, memory) per faculty.
     * If facultyId provided as path parameter, returns only the resources assigned to specific faculty, if it exists
     * in the database.
     *
     * @param facultyId the facultyId to get the assigned resources to.
     *
     * @return response entity containing a list of all relevant resources with facultyIds (or one FacultyTotalResource
     * object if facultyId specified).
     */
    @GetMapping(value = {"/resources/assigned", "/resources/assigned/{facultyId}"})
    @PreAuthorize("hasAnyRole('SYSADMIN', 'FACULTY')")
    public ResponseEntity<List<FacultyResourcesResponseModel>> getResourcesAssignedToFaculty(
        @RequestHeader HttpHeaders headers, @PathVariable(value = "facultyId", required = false) String facultyId) {
        // check whether the user actually can make this request
        if (!this.privilegeVerificationService.verifyAccountOfCorrectFaculty(headers, facultyId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (facultyId == null) {
            var rawResources = this.schedulingDataProcessingService.getAssignedResourcesPerFaculty();
            return ResponseEntity.ok(FacultyResourcesResponseModel
                .convertAllFacultyTotalResourcesToResponseModels(rawResources));
        } else if (this.nodeDataProcessingService.existsByFacultyId(facultyId)) {
            var rawResources = List.of(this.schedulingDataProcessingService
                .getAssignedResourcesForGivenFaculty(facultyId));
            return ResponseEntity.ok(FacultyResourcesResponseModel
                .convertAllFacultyTotalResourcesToResponseModels(rawResources));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Gets and returns the total reserved resources per day per faculty.
     *
     * @param rawDate the date on which to look for reserved resources, in String format.
     * @param facultyId the facultyId to find reserved resources of.
     *
     * @return response entity containing a list of response models containing the reserved resources
     * in the three categories, as well as the date and the facultyId.
     */
    @GetMapping(value = {"/resources/reserved", "/resources/reserved/{date}&{facultyId}",
        "/resources/reserved/{date}&", "/resources/reserved/&{facultyId}", "resources/reserved/&"})
    @PreAuthorize("hasAnyRole('SYSADMIN', 'FACULTY')")
    public ResponseEntity<List<FacultyDatedResourcesResponseModel>> getReservedResourcesPerFacultyPerDay(
        @RequestHeader HttpHeaders headers,
        @PathVariable(value = "date", required = false) String rawDate,
        @PathVariable(value = "facultyId", required = false) String facultyId) {
        // check whether the user actually can make this request
        if (!this.privilegeVerificationService.verifyAccountOfCorrectFaculty(headers, facultyId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        LocalDate date = rawDate != null ? LocalDate.parse(rawDate) : null;
        if (date == null && facultyId == null) {
            // for all dates, for all faculties
            return ResponseEntity.ok(FacultyDatedResourcesResponseModel
                .convertToResponseModels(this.schedulingDataProcessingService
                    .getReservedResourcesPerFacultyPerDay()));
        } else if (date != null && facultyId == null) {
            if (!this.schedulingDataProcessingService.existsInScheduleByScheduledFor(date)) {
                return ResponseEntity.badRequest().build();
            }

            // for given date, for all faculties
            return ResponseEntity.ok(FacultyDatedResourcesResponseModel
                .convertToResponseModels(this.schedulingDataProcessingService
                    .getReservedResourcesPerFacultyForGivenDay(date)));
        } else if (date == null && facultyId != null) {
            if (!this.schedulingDataProcessingService.existsInScheduleByFacultyId(facultyId)) {
                return ResponseEntity.badRequest().build();
            }

            // for given faculty, for all dates
            return ResponseEntity.ok(FacultyDatedResourcesResponseModel
                .convertToResponseModels(this.schedulingDataProcessingService
                    .getReservedResourcesPerDayForGivenFaculty(facultyId)));
        } else {
            if (!this.schedulingDataProcessingService.existsInScheduleByScheduledFor(date)
                || !this.schedulingDataProcessingService.existsInScheduleByFacultyId(facultyId)) {
                return ResponseEntity.badRequest().build();
            }

            // for given faculty, for given date
            return ResponseEntity.ok(FacultyDatedResourcesResponseModel
                .convertToResponseModels(this.schedulingDataProcessingService
                    .getReservedResourcesForGivenDayForGivenFaculty(date, facultyId)));
        }
    }

    /**
     * Gets and returns the total available resources per day per faculty. U
     *
     * @param rawDate the date on which to look for available resources, in String format.
     * @param facultyId the facultyId to find available resources of.
     *
     * @return response entity containing a list of response models containing the available resources
     * in the three categories, as well as the date and the facultyId.
     */
    @GetMapping(value = {"/resources/available", "/resources/available/{date}&{facultyId}",
        "/resources/available/{date}&", "/resources/available/&{facultyId}", "resources/available/&"})
    public ResponseEntity<List<FacultyDatedResourcesResponseModel>> getAvailableResourcesPerFacultyPerDay(
        @RequestHeader HttpHeaders headers,
        @PathVariable(value = "date", required = false) String rawDate,
        @PathVariable(value = "facultyId", required = false) String facultyId) {
        LocalDate date = rawDate != null ? LocalDate.parse(rawDate) : null;
        if (!this.privilegeVerificationService.verifyAccountCorrectPrivilegesForDayAndFaculty(headers, facultyId,
            date)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // functionality
        if (date == null && facultyId == null) {
            // for all dates, for all faculties
            return ResponseEntity.ok(this.schedulingDataProcessingService.getAvailableResourcesForAllFacultiesForAllDays());
        } else if (date != null && facultyId == null) {

            // for given date, for all faculties
            return ResponseEntity.ok(this.schedulingDataProcessingService
                .getAvailableResourcesForAllFacultiesForGivenDay(date));
        } else if (date == null && facultyId != null) {
            if (!this.schedulingDataProcessingService.existsInScheduleByFacultyId(facultyId)) {
                return ResponseEntity.badRequest().build();
            }

            // for given faculty, for all dates
            return ResponseEntity.ok(this.schedulingDataProcessingService
                .getAvailableResourcesForGivenFacultyForAllDays(facultyId));
        } else {
            if (!this.schedulingDataProcessingService.existsInScheduleByFacultyId(facultyId)) {
                return ResponseEntity.badRequest().build();
            }

            // for given faculty, for given date
            return ResponseEntity.ok(this.schedulingDataProcessingService
                .getAvailableResourcesForGivenFacultyForGivenDay(date, facultyId));
        }
    }

    /**
     * Gets and returns the available resources for the given faculty between tomorrow and the given date, inclusive.
     *
     * @param rawDate the String form of the date until which to calculate available resources.
     * @param facultyId the facultyId of the faculty to calculate available resources for.
     *
     * @return response entity containing a list of available resources per day from tomorrow until given.
     */
    @GetMapping(value = "/resources/availableUntil/{date}/{facultyId}")
    public ResponseEntity<List<FacultyResourcesResponseModel>> getAvailableResourcesForGivenFacultyBeforeGivenDate(
        @PathVariable("date") String rawDate, @PathVariable("facultyId") String facultyId) {
        // anti-corruption
        try {
            LocalDate.parse(rawDate);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().build();
        }
        LocalDate date = LocalDate.parse(rawDate);
        if (date.isBefore(this.dateProvider.getTomorrow())
            || !this.nodeDataProcessingService.existsByFacultyId(facultyId)) {
            return ResponseEntity.badRequest().build();
        }
        var rawResources = this.schedulingDataProcessingService
            .getAvailableResourcesForGivenFacultyUntilDay(facultyId, date);
        return ResponseEntity.ok(FacultyResourcesResponseModel
            .convertForRequestService(rawResources, facultyId));
    }
}