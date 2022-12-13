package nl.tudelft.sem.template.authentication.controllers;

import java.util.List;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.NetId;
import nl.tudelft.sem.template.authentication.domain.user.RegistrationService;
import nl.tudelft.sem.template.authentication.models.ApplyFacultyRequestModel;
import nl.tudelft.sem.template.authentication.models.GetFacultyRequestModel;
import nl.tudelft.sem.template.authentication.models.GetFacultyResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * CLass controller which handles faculty related communication.
 */
@RestController
public class FacultyController {

    private final transient RegistrationService registrationService;

    @Autowired
    public FacultyController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /**
     * Endpoint to add faculty to user.
     *
     * @param request request from user with the data
     * @return 200 OK if the faculty is added to the user
     * @throws Exception if a user or the faculty  doesn't exist
     */
    @PostMapping("/applyFaculty")
    public ResponseEntity applyFaculty(@RequestBody ApplyFacultyRequestModel request) throws Exception {

        try {
            AppUser.Faculty faculty = AppUser.Faculty.valueOf(request.getFaculty());
            NetId netId = new NetId(request.getNetId());
            registrationService.applyFacultyUser(netId, faculty);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return ResponseEntity.ok("Faculty added");
    }

    /**
     * Gets all the faculties of the user.
     *
     * @param request request from user with the data
     * @return JSON of al the request in one field
     * @throws Exception if a user does not exist
     */
    @PostMapping("/getUserFaculties")
    public ResponseEntity getFaculties(@RequestBody GetFacultyRequestModel request) throws Exception {
        String response;
        try {
            NetId netId = new NetId(request.getNetId());
            List<AppUser.Faculty> facultyList = registrationService.getFaculties(netId);
            response = facultyList.toString();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return ResponseEntity.ok(new GetFacultyResponseModel(response));
    }

    /**
     * Removes faculty from a user.
     *
     * @param request request from user with the data
     * @return 200 OK if faculty is removed
     * @throws Exception if a user or faculty does not exist
     */
    @PostMapping("/removeFaculty")
    public ResponseEntity removeFaculties(@RequestBody ApplyFacultyRequestModel request) throws Exception {
        try {
            AppUser.Faculty faculty = AppUser.Faculty.valueOf(request.getFaculty());
            NetId netId = new NetId(request.getNetId());
            registrationService.removeFacultyUser(netId, faculty);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return ResponseEntity.ok("Faculty removed");
    }
}
