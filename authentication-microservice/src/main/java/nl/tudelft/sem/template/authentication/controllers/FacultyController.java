package nl.tudelft.sem.template.authentication.controllers;

import java.util.Arrays;
import java.util.List;
import nl.tudelft.sem.template.authentication.authentication.JwtTokenVerifier;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.NetId;
import nl.tudelft.sem.template.authentication.models.ApplyFacultyRequestModel;
import nl.tudelft.sem.template.authentication.models.GetFacultyRequestModel;
import nl.tudelft.sem.template.authentication.models.GetFacultyResponseModel;
import nl.tudelft.sem.template.authentication.services.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    private final transient JwtTokenVerifier tokenVerifier;

    @Autowired
    public FacultyController(RegistrationService registrationService,
                             JwtTokenVerifier tokenVerifier) {
        this.registrationService = registrationService;
        this.tokenVerifier = tokenVerifier;
    }

    /**
     * Endpoint to add faculty to user.
     *
     * @param request request from user with the data
     * @return 200 OK if the faculty is added to the user
     * @throws ResponseStatusException if a user or the faculty  doesn't exist
     */
    @PostMapping("/applyFaculty")
    public ResponseEntity<String> applyFaculty(@RequestBody ApplyFacultyRequestModel request)
            throws ResponseStatusException {

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
     * @throws ResponseStatusException if a user does not exist
     */
    @PostMapping("/getUserFaculties")
    public ResponseEntity<GetFacultyResponseModel> getFaculties(@RequestBody GetFacultyRequestModel request)
            throws ResponseStatusException {

        try {
            NetId netId = new NetId(tokenVerifier.getNetIdFromToken(request.getToken()));
            List<AppUser.Faculty> facultyList = registrationService.getFaculties(netId);
            return ResponseEntity.ok(new GetFacultyResponseModel(facultyList.toString()));
            //response = facultyList.toString();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Removes faculty from a user.
     *
     * @param request request from user with the data
     * @return 200 OK if faculty is removed
     * @throws ResponseStatusException if a user or faculty does not exist
     */
    @DeleteMapping("/removeFaculty")
    public ResponseEntity<String> removeFaculties(@RequestBody ApplyFacultyRequestModel request)
            throws ResponseStatusException {
        try {
            AppUser.Faculty faculty = AppUser.Faculty.valueOf(request.getFaculty());
            NetId netId = new NetId(request.getNetId());
            registrationService.removeFacultyUser(netId, faculty);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return ResponseEntity.ok("Faculty removed");
    }

    /**
     * Method to retrieve all existing faculties that the University contains.
     *
     * @return List of Strings of all the faculties
     * @throws ResponseStatusException when there is an error retrieving the faculties
     */
    @GetMapping("/getAllFaculties")
    public ResponseEntity<List<String>> getAllFaculties()
            throws ResponseStatusException {
        try {
            List<String> enumValues = List.of(Arrays.toString(AppUser.Faculty.values()));
            return ResponseEntity.ok(enumValues);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
