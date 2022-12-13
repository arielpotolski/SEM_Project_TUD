package nl.tudelft.sem.template.authentication.controllers;

import java.util.List;
import nl.tudelft.sem.template.authentication.authentication.JwtTokenGenerator;
import nl.tudelft.sem.template.authentication.authentication.JwtUserDetailsService;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.NetId;
import nl.tudelft.sem.template.authentication.domain.user.Password;
import nl.tudelft.sem.template.authentication.domain.user.RegistrationService;
import nl.tudelft.sem.template.authentication.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;



@RestController
public class AuthenticationController {

    private final transient AuthenticationManager authenticationManager;

    private final transient JwtTokenGenerator jwtTokenGenerator;

    private final transient JwtUserDetailsService jwtUserDetailsService;

    private final transient RegistrationService registrationService;

    /**
     * Instantiates a new UsersController.
     *
     * @param authenticationManager the authentication manager
     * @param jwtTokenGenerator     the token generator
     * @param jwtUserDetailsService the user service
     * @param registrationService   the registration service
     */
    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager,
                                    JwtTokenGenerator jwtTokenGenerator,
                                    JwtUserDetailsService jwtUserDetailsService,
                                    RegistrationService registrationService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenGenerator = jwtTokenGenerator;
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.registrationService = registrationService;
    }

    /**
     * Endpoint for authentication.
     *
     * @param request The login model
     * @return JWT token if the login is successful
     * @throws Exception if the user does not exist or the password is incorrect
     */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponseModel> authenticate(@RequestBody AuthenticationRequestModel request)
            throws Exception {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getNetId(),
                            request.getPassword()));
        } catch (DisabledException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", e);
        }

        final UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(request.getNetId());
        final String jwtToken = jwtTokenGenerator.generateToken(userDetails);
        return ResponseEntity.ok(new AuthenticationResponseModel(jwtToken));
    }

    /**
     * Endpoint for registration.
     *
     * @param request The registration model
     * @return 200 OK if the registration is successful
     * @throws Exception if a user with this netid already exists
     */
    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegistrationRequestModel request) throws Exception {

        try {
            NetId netId = new NetId(request.getNetId());
            Password password = new Password(request.getPassword());
            registrationService.registerUser(netId, password);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint for changing password.
     *
     * @param request request from user with the data
     * @return 200 OK if the change is successful
     * @throws Exception if a user does not exist
     */
    @PostMapping("/change")
    public  ResponseEntity change(@RequestBody RegistrationRequestModel request) throws Exception {
        try {
            NetId netId = new NetId(request.getNetId());
            Password password = new Password(request.getPassword());
            registrationService.changePassword(netId, password);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return ResponseEntity.ok("Password is" + request.getPassword());
    }


    @GetMapping("/hello")
    public ResponseEntity<String> helloWorld() {
        return ResponseEntity.ok("Hello ");

    }


}
