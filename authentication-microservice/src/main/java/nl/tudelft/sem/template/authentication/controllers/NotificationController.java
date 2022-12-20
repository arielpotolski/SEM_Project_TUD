package nl.tudelft.sem.template.authentication.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.List;
import nl.tudelft.sem.template.authentication.authtemp.AuthManager;
import nl.tudelft.sem.template.authentication.communicationdata.Notification;
import nl.tudelft.sem.template.authentication.models.NotificationRequestModel;
import nl.tudelft.sem.template.authentication.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Class controller for receiving notifications.
 */
@RestController
public class NotificationController {

    private final transient AuthManager authManager;
    private final transient NotificationService notificationService;

    /**
     * Instantiates a new controller.
     *
     * @param authManager Spring Security component used to authenticate and authorize the user
     */
    @Autowired
    public NotificationController(AuthManager authManager, NotificationService notificationService) {
        this.authManager = authManager;
        this.notificationService = notificationService;
    }

    /**
     * Endpoint for receiving any type of notification from other microservices.
     *
     * @param data All data of the notification
     * @return ResponseEntity containing success
     * @throws ResponseStatusException when the request fails
     */
    @PostMapping("/notification")
    public ResponseEntity<String> receiveJobNotification(
            @RequestBody NotificationRequestModel data) {
        try {
            Notification notificationData = Notification.createNotification(data);
            notificationService.addNotification(notificationData);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return ResponseEntity.ok("successfully received Notification");
    }

    /**
     * Method a user can invoke to receive all notifications they have.
     *
     * @return ResponseEntity when request success
     * @throws ResponseStatusException when bad request
     */
    @GetMapping("/getNotification")
    public ResponseEntity<String> sendJobNotifications() {

        try {
            String netId = authManager.getNetId();
            List<Notification> l = notificationService.getNotifications(netId);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(l);
            return ResponseEntity.ok(json);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }









}
