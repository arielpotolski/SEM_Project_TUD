package nl.tudelft.sem.template.example.controllers;

import nl.tudelft.sem.template.example.authentication.AuthManager;
import nl.tudelft.sem.template.example.domain.Request;
import nl.tudelft.sem.template.example.services.RequestAllocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Hello World example controller.
 * <p>
 * This controller shows how you can extract information from the JWT token.
 * </p>
 */
@RestController
@RequestMapping("/job")
public class JobRequestController {

    private final transient AuthManager authManager;
    private final RequestAllocationService requestAllocationService;

    /**
     * Instantiates a new controller.
     *
     * @param authManager Spring Security component used to authenticate and authorize the user
     * @param requestAllocationService
     */
    @Autowired
    public JobRequestController(AuthManager authManager, RequestAllocationService requestAllocationService) {
        this.authManager = authManager;
        this.requestAllocationService = requestAllocationService;
    }

    /**
     * Gets example by id.
     *
     * @return the example found in the database with the given id
     */
    @GetMapping("/hello")
    public ResponseEntity<String> helloWorld() {
        return ResponseEntity.ok("Hello " + authManager.getNetId());

    }

    @PostMapping("/sendRequest")
    public ResponseEntity<Request> sendRequest(@RequestBody Request request){
        System.out.println(request);
        if(requestAllocationService.enoughResourcesForJob(request)){
            requestAllocationService.sendRequestToCluster(request);
        }
        // Possibly notify the user if there are no available resources

        return ResponseEntity.ok()
                .body(request);
    }


}
