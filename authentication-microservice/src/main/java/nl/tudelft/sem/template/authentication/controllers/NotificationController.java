package nl.tudelft.sem.template.authentication.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import nl.tudelft.sem.template.authentication.authtemp.AuthManager;
import nl.tudelft.sem.template.authentication.communicationData.Notification;
import nl.tudelft.sem.template.authentication.models.NotificationRequestModel;
import nl.tudelft.sem.template.authentication.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;


@RestController
public class NotificationController {

    private final transient AuthManager authManager;
    private final NotificationService notificationService;

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
     * Gets example by id.
     *
     * @return the example found in the database with the given id
     */
    @GetMapping("/hello")
    public ResponseEntity<String> helloWorld() {
        return ResponseEntity.ok("Hello " + authManager.getNetId());

    }
    @PostMapping("/notification")
    public ResponseEntity<String> receiveJobNotification(
            @RequestBody NotificationRequestModel data)
    {
        String netId = data.getNetId();
        Notification notificationData = Notification.createNotification(data);
        notificationService.addNotification(netId, notificationData);
        return ResponseEntity.ok("successfully received Notification");
    }

    @GetMapping("/getNotification")
    public ResponseEntity<String> sendJobNotifications(){
        String netId = authManager.getNetId();
        List<Notification> l = notificationService.getNotifications(netId);

        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(l);
            return ResponseEntity.ok(json);
        }catch (Exception e){
            System.out.println("error creating JobNotification JSON" + e);
        }
        return ResponseEntity.ok("This should never be returned!");

    }









}
