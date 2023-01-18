package nl.tudelft.sem.template.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import nl.tudelft.sem.template.example.authentication.AuthManager;
import nl.tudelft.sem.template.example.domain.ClockUser;
import nl.tudelft.sem.template.example.domain.Request;
import nl.tudelft.sem.template.example.domain.RequestRepository;
import nl.tudelft.sem.template.example.services.RequestAllocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
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
public class SendingRequestsController {

    private final transient AuthManager authManager;
    private final RequestAllocationService requestAllocationService;
    private final RequestRepository requestRepository;
    private final ClockUser clockUser;
    private final ApprovingRequestsController approvingRequestsController;

    /**
     * Instantiates a new controller.
     *
     * @param authManager              Spring Security component used to authenticate and authorize the user
     * @param requestAllocationService the request allocation service
     * @param requestRepository        the request repository
     * @param clockUser                clock that can be configurable
     * @param approvingRequestsController  controller for approving requests
     */
    @Autowired
    public SendingRequestsController(AuthManager authManager, RequestAllocationService requestAllocationService,
                                     RequestRepository requestRepository, ClockUser clockUser,
                                     ApprovingRequestsController approvingRequestsController) {
        this.authManager = authManager;
        this.requestAllocationService = requestAllocationService;
        this.requestRepository = requestRepository;
        this.clockUser = clockUser;
        this.approvingRequestsController = approvingRequestsController;
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
        approvingRequestsController.publishRequest();
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


}