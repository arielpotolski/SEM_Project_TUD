package nl.tudelft.sem.template.authentication.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import nl.tudelft.sem.template.authentication.authtemp.AuthManager;
import nl.tudelft.sem.template.authentication.communicationdata.Notification;
import nl.tudelft.sem.template.authentication.models.DeleteNotificationRequestModel;
import nl.tudelft.sem.template.authentication.models.GetNotifactionsRequestModel;
import nl.tudelft.sem.template.authentication.models.NotificationRequestModel;
import nl.tudelft.sem.template.authentication.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
    @PreAuthorize("hasAnyRole('FACULTY', 'SYSADMIN', 'SYSTEM')")
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
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public ResponseEntity<String> sendJobNotifications(@RequestBody (required = false) GetNotifactionsRequestModel request) {
        try {
            request.check();
            LocalDate begin = LocalDate.now();
            LocalDate end = LocalDate.MIN;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            formatter = formatter.localizedBy(Locale.ENGLISH);

            try {
                begin = LocalDate.parse(request.getDateUntil(), formatter);
            } catch (Exception e) {
                System.out.println("No Start in body");
            }
            try {
                end = LocalDate.parse(request.getDateFrom(), formatter);
            } catch (Exception e) {
                System.out.println("No End in body");
            }
            String netId = authManager.getNetId();
            List<Notification> resultList = notificationService.getNotificationsWithDate(netId, begin, end);
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(resultList);
            return ResponseEntity.ok(json);
        } catch (Exception e) {
            System.out.println("No Request Body");
        }


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

    /**
     * End point for deleting a notification.
     *
     * @param request the Data (Id) from the user
     * @return the response it is supposed to give
     */
    @DeleteMapping("/DeleteNotification")
    public ResponseEntity<String> deleteNotification(@RequestBody DeleteNotificationRequestModel request) {
        try {
            long id = request.getId();
            String netId = authManager.getNetId();
            Notification notification = notificationService.getNotificationById(id);
            if (notification.getNetId().equals(netId)) {
                notificationService.deleteNotifications(id);
            } else {
                throw new IllegalArgumentException();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return ResponseEntity.ok("Notification is deleted");
    }

}
