package nl.tudelft.sem.template.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * This is the controller which manages all incoming communication from other services
 *
 */
@RestController
@RequestMapping("/job")
public class JobRequestController {

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
    public JobRequestController(AuthManager authManager, RequestAllocationService requestAllocationService,
                                RequestRepository requestRepository) {
        this.authManager = authManager;
        this.requestAllocationService = requestAllocationService;
        this.requestRepository = requestRepository;
    }


    /**
     * This mapping is responsible for receiving requests from users and processing them afterwards.
     *
     * @param headers the headers
     * @param request the request
     * @return the response entity
     */
    @PostMapping("/sendRequest")
    public ResponseEntity<String> sendRequest(@RequestHeader HttpHeaders headers, @RequestBody Request request){

        //check if the user is from the corresponding faculty

        if(request.getFaculty() == null){
            return ResponseEntity.ok()
                    .body("You are not verified to send requests to this faculty");
        }

        List<String> token = headers.get("token");

        List<String> facultyUserFaculties = requestAllocationService.getFacultyUserFaculties(token.get(0));

        if(facultyUserFaculties.contains(request.getFaculty())){
            request.setApproved(false);
            requestRepository.save(request);
            publishRequest();

            return ResponseEntity.ok()
                    .body("The request was sent. Now it is to be approved by faculty.");
        }

        return ResponseEntity.ok()
                .body("You are not verified to send requests to this faculty");

    }

    /**
     * This endpoint is responsible for broadcasting all pending requests, so a faculty-privileged account can approve/decline them.
     *
     * @return the response entity
     */
    @GetMapping("pendingRequests")
    public ResponseEntity<List<Request>> publishRequest(){
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
    public ResponseEntity<List<Request>> sendApprovals(@RequestHeader HttpHeaders headers, @RequestBody ApprovalInformation approvalInformation) throws JsonProcessingException {
        //I require a file with the ids of all approved requests, check if the sender is with a faculty profile

        List<String> facultiesOfFacultyUser = requestAllocationService.getFacultyUserFaculties(headers.get("token").get(0));

        List<Request> requests = requestRepository.findAll().stream()
                .filter(x -> Utils.idIsContained(approvalInformation.getIds(), x.getId()))
                .filter(x->facultiesOfFacultyUser.contains(x.getFaculty()))
                .collect(Collectors.toList());

        for (Request request : requests) {
            request.setApproved(true);
        }

        //Implementation of changing the status of respective requests to approved

        for (Request request : requests) {
            if(requestAllocationService.enoughResourcesForJob(request)){
                requestAllocationService.sendRequestToCluster(request);

            }

            // Users team must open an endpoint for post requests, notifying for declined requests
            requestAllocationService.sendDeclinedRequestToUserService(request);
        }

        // Deleting approved and sent entities
        requestRepository.deleteAll(requests);

        return ResponseEntity.ok().body(requests);

    }

}
