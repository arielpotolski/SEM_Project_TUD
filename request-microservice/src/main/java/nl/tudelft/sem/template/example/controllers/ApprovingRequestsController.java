package nl.tudelft.sem.template.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.example.authentication.AuthManager;
import nl.tudelft.sem.template.example.domain.ApprovalInformation;
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
public class ApprovingRequestsController {

    private final transient AuthManager authManager;
    private final RequestAllocationService requestAllocationService;
    private final RequestRepository requestRepository;


    /**
     * Instantiates a new controller.
     *
     * @param authManager              Spring Security component used to authenticate and authorize the user
     * @param requestAllocationService the request allocation service
     * @param requestRepository        the request repository
     */
    @Autowired
    public ApprovingRequestsController(AuthManager authManager, RequestAllocationService requestAllocationService,
                                       RequestRepository requestRepository) {
        this.authManager = authManager;
        this.requestAllocationService = requestAllocationService;
        this.requestRepository = requestRepository;

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
