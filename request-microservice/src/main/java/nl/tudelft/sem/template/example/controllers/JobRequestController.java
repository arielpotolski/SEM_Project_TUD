package nl.tudelft.sem.template.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
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

    private final RequestAllocationService requestAllocationService;
    private final RequestRepository requestRepository;
    private final ClockUser clockUser;

    /**
     * Instantiates a new controller.
     *
     * @param requestAllocationService the request allocation service
     * @param requestRepository        the request repository
     * @param clockUser                clock that can be configurable
     */
    @Autowired
    public JobRequestController(RequestAllocationService requestAllocationService,
                                RequestRepository requestRepository, ClockUser clockUser) {
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
    public ResponseEntity<String> sendRequest(@RequestHeader HttpHeaders headers,
                                              @RequestBody Request request) throws JsonProcessingException {

        if (request.getFaculty() == null) {
            return ResponseEntity.ok()
                    .body("You are not verified to send requests to this faculty");
        }
        String token = headers.get("authorization").get(0).replace("Bearer ", "");
        return getResponseEntity(token, request);

    }

    /**
     * Method to get the correct responseEnity from the user's request information.
     *
     * @param token token of the user account.
     * @param request request of the user containing all information.
     *
     * @return ResponseEntity following the information
     * @throws JsonProcessingException when error.
     */
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    private ResponseEntity<String> getResponseEntity(String token, Request request)
            throws JsonProcessingException {
        int shortTerm = 5;
        int longTerm = 360;
        long minutes = getMinutes();
        List<String> facultyUserFaculties = requestAllocationService.getFacultyUserFaculties(token);
        if (clockUser.getTimeLd().isEqual(request.getPreferredDate())) {
            return ResponseEntity.ok().body("You cannot send requests for the same day.");
        } else if (minutes <= shortTerm) {
            return ResponseEntity.ok().body("You cannot send requests 5 min before the following day.");
        } else if (minutes <= longTerm) {
            if (requestAllocationService.enoughResourcesForJob(request, token)
                    && request.getPreferredDate().equals(clockUser.getTimeLd().plusDays(1L))
                    && (facultyUserFaculties.contains(request.getFaculty()))) {
                setSaveAndPublishRequest(request, true);
                this.requestAllocationService.sendRequestToCluster(request, token);
                return ResponseEntity.ok()
                        .body("The request is automatically forwarded "
                                + "and will be completed if there are sufficient resources");
            }
            setSaveAndPublishRequest(request, false);
            return ResponseEntity.ok()
                    .body("Request forwarded, "
                            + "but resources are insufficient or preferred date is not tomorrow");
        } else if (facultyUserFaculties.contains(request.getFaculty())) {
            setSaveAndPublishRequest(request, false);
            return ResponseEntity.ok()
                    .body("The request was sent. Now it is to be approved by faculty.");
        } else {
            return ResponseEntity.ok()
                    .body("You are not assigned to this faculty.");
        }
    }

    /**
     * Method which sets the request to either approved or rejected, saves them to the repository,
     * and lastly publishes them for further processing.
     *
     * @param request Request in question
     * @param approved if it is approved or not
     */
    private void setSaveAndPublishRequest(Request request, boolean approved) {
        request.setApproved(approved);
        requestRepository.save(request);
        publishRequest();
    }

    /**
     * Method to calculate the amount of minutes the request until the next day.
     *
     * @return minutes until the next day,
     */
    private long getMinutes() {

        LocalDateTime d1 = clockUser.getTimeLdt();
        LocalDate d2 = clockUser.getTimeLd().plusDays(1L);
        LocalDateTime ref = d2.atStartOfDay();

        return d1.until(ref, ChronoUnit.MINUTES);
    }

    /**
     * This endpoint is responsible for broadcasting all pending requests,
     * so a faculty-privileged account can approve/decline them.
     *
     * @return the response entity
     */
    @GetMapping("pendingRequests")
    @PreAuthorize("hasRole('FACULTY')")
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
     * @return                    the response entity
     * @throws JsonProcessingException the json processing exception
     */
    @PostMapping("sendApprovals")
    @PreAuthorize("hasRole('FACULTY')")
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

        handleRequests(requests, token);
        return ResponseEntity.ok().body(requests);

    }


    /**
     * Synthesizes and extracts all the request logic from the sendApprovals method.
     * It is done for better code quality and readability.
     *
     * @param requests list of requests
     * @param token token of the current user
     * @throws JsonProcessingException the json processing exception
     */
    public void handleRequests(List<Request> requests, String token)
            throws JsonProcessingException {

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
    }

}
