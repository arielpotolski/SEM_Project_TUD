package nl.tudelft.sem.template.authentication.controllers;

import nl.tudelft.sem.template.authentication.authtemp.AuthManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Class which will handle assigning Employees to Faculties
 */
@RestController
public class EmployeeController {

    private final transient AuthManager authManager;

    /**
     * Instantiates a new controller.
     *
     * @param authManager Spring Security component used to authenticate and authorize the user
     */
    @Autowired
    public EmployeeController(AuthManager authManager) {
        this.authManager = authManager;
    }

    /**
     * Gets example by id.
     *
     * @return the example found in the database with the given id
     */
    @GetMapping("/hello2")
    public ResponseEntity<String> helloWorld() {
        return ResponseEntity.ok("Employee has been added");

    }
}

