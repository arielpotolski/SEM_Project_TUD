package nl.tudelft.sem.template.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.example.authentication.AuthManager;
import nl.tudelft.sem.template.example.domain.ApprovalInformation;
import nl.tudelft.sem.template.example.domain.ClockUser;
import nl.tudelft.sem.template.example.domain.Request;
import nl.tudelft.sem.template.example.domain.RequestRepository;
import nl.tudelft.sem.template.example.services.RequestAllocationService;
import nl.tudelft.sem.template.example.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * This is the controller which manages all incoming communication from other services.
 */
@RestController
@RequestMapping("/job")
@SuppressWarnings("PMD.BeanMembersShouldSerialize")
public class JobRequestController {

    private final transient AuthManager authManager;
    private final RequestAllocationService requestAllocationService;
    private final RequestRepository requestRepository;
    private final ClockUser clockUser;

    /**
     * Instantiates a new controller.
     *  @param authManager              Spring Security component used to authenticate and authorize the user
     * @param requestAllocationService the request allocation service
     * @param requestRepository        the request repository
     * @param clockUser                clock that can be configurable
     */
    @Autowired
    public JobRequestController(AuthManager authManager, RequestAllocationService requestAllocationService,
                                RequestRepository requestRepository, ClockUser clockUser) {
        this.authManager = authManager;
        this.requestAllocationService = requestAllocationService;
        this.requestRepository = requestRepository;
        this.clockUser = clockUser;
    }


    /**
     * This mapping is responsible for receiving requests from users and processing them afterwards.
     *
     * @param headers the headers
     * @param request the request
     * @return the response entity
     */
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    @PostMapping("/sendRequest")
    public ResponseEntity<String> sendRequest(@RequestHeader HttpHeaders headers, @RequestBody Request request) {

        Clock clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"));
        clockUser.setClock(clock);

        //check if the user is from the corresponding faculty

        if (request.getFaculty() == null) {
            return ResponseEntity.ok()
                    .body("You are not verified to send requests to this faculty");
        }

        LocalDateTime preferredDate = request.getPreferredDate().atStartOfDay(ZoneId.systemDefault())
                .toLocalDateTime();
        LocalDate onlyDate = request.getPreferredDate();

        LocalDateTime d1 = clockUser.getTimeLDT();
        LocalDate d2 = clockUser.getTimeLD().plusDays(1L);
        LocalDateTime ref = d2.atStartOfDay();

        int timeLimit1 = 5;
        int timeLimit2 = 360;

        String token = headers.get("authorization").get(0).replace("Bearer ", "");
        List<String> facultyUserFaculties = requestAllocationService.getFacultyUserFaculties(token);

        long minutes = d1.until(ref, ChronoUnit.MINUTES);

        if (d2.isEqual(onlyDate)) {
            return ResponseEntity.ok()
                    .body("You cannot send requests for the same day.");
        } else if (!d2.isEqual(onlyDate)) {
            if (minutes <= timeLimit1) {
                return ResponseEntity.ok()
                        .body("You cannot send requests 5 min before the following day.");

            } else if (minutes <= timeLimit2) {
                if (requestAllocationService.enoughResourcesForJob(request, token) && onlyDate.equals(d2)) {    //LocalDateTime and LocalDate diff

                    if (facultyUserFaculties.contains(request.getFaculty())) {
                        request.setApproved(true);                        // Doesn't require approval; First come, first served
                        requestRepository.save(request);
                        publishRequest();

                        return ResponseEntity.ok()
                                .body("The request is automatically forwarded and will be completed if there are sufficient resources");
                    }

                } else {
                    return ResponseEntity.ok()
                            .body("Request forwarded, but resources are insufficient");
                }
            }
        }

        if (facultyUserFaculties.contains(request.getFaculty())) {
            request.setApproved(false);
            requestRepository.save(request);
            publishRequest();

            return ResponseEntity.ok()
                    .body("The request was sent. Now it is to be approved by faculty.");
        }

        return ResponseEntity.ok()
                .body("You are not assigned to this faculty.");

    }

    /**
     * This endpoint is responsible for broadcasting all pending requests,
     * so a faculty-privileged account can approve/decline them.
     *
     * @return the response entity
     */
    @GetMapping("pendingRequests")
    @PreAuthorize("hasAnyRole('SYSADMIN', 'FACULTY')")
    public ResponseEntity<List<Request>> publishRequest() {
        List<Request> requests = requestRepository.findAllByApprovedIs(false);
        return ResponseEntity.status(HttpStatus.OK).body(requests);
    }

    /**
     * This endpoint is responsible for accepting the ids of approved requests,
     * and see if the sender is legitimate and from the respective faculty.
     * Afterwards, the requests are sent to the cluster.
     *
     * @param headers             the headers
     * @param approvalInformation the approval information
     * @return the response entity
     * @throws JsonProcessingException the json processing exception
     */
    @PostMapping("sendApprovals")
    @PreAuthorize("hasAnyRole('SYSADMIN', 'FACULTY')")
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public ResponseEntity<List<Request>> sendApprovals(@RequestHeader HttpHeaders headers,
                                                       @RequestBody ApprovalInformation approvalInformation)
            throws JsonProcessingException {
        //I require a file with the ids of all approved requests, check if the sender is with a faculty profile

        String token = headers.get("Authorization").get(0).replace("Bearer ", "");
        List<String> facultiesOfFacultyUser = requestAllocationService
                .getFacultyUserFaculties(token);

        List<Request> requests = requestRepository.findAll().stream()
                .filter(x -> Utils.idIsContained(approvalInformation.getIds(), x.getId()))
                .filter(x -> facultiesOfFacultyUser.contains(x.getFaculty()))
                .collect(Collectors.toList());

        for (Request request : requests) {
            request.setApproved(true);
        }

        //Implementation of changing the status of respective requests to approved

        for (Request request : requests) {
            if (requestAllocationService.enoughResourcesForJob(request, token)) {
                requestAllocationService.sendRequestToCluster(request, token);
            } else {
                // Users team must open an endpoint for post requests, notifying for declined requests
                requestAllocationService.sendDeclinedRequestToUserService(request, token);
            }
        }

        // Deleting approved and sent entities
        requestRepository.deleteAll(requests);

        return ResponseEntity.ok().body(requests);

    }

}
