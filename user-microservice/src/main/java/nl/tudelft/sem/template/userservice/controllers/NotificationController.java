package nl.tudelft.sem.template.userservice.controllers;

import nl.tudelft.sem.template.userservice.authentication.AuthManager;
import nl.tudelft.sem.template.userservice.communicationUtil.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class NotificationController {

    private final transient AuthManager authManager;

    /**
     * Instantiates a new controller.
     *
     * @param authManager Spring Security component used to authenticate and authorize the user
     */
    @Autowired
    public NotificationController(AuthManager authManager) {
        this.authManager = authManager;
    }

    /**
     * Gets example by id.
     *
     * @return the example found in the database with the given id
     */
    @GetMapping("/hello")
    public ResponseEntity<String> helloWorld() {
        return ResponseEntity.ok("Employee has been added");

    }
    @PostMapping("/jobNotification")
    public ResponseEntity<String> receiveJobNotification(
            @RequestParam("State") String state
    ){

        return ResponseEntity.ok("a");
    }





}
