package nl.tudelft.sem.template.userservice.controllers;

import nl.tudelft.sem.template.userservice.authentication.AuthManager;
import nl.tudelft.sem.template.userservice.communicationData.Data;
import nl.tudelft.sem.template.userservice.communicationData.JobNotificationData;
import nl.tudelft.sem.template.userservice.communicationUtil.*;
import nl.tudelft.sem.template.userservice.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.util.Arrays;
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
        return ResponseEntity.ok("Employee has been added");

    }
    @PostMapping("/jobNotification")
    public ResponseEntity<String> receiveJobNotification(
            @RequestParam("state") String state,
            @RequestParam("date") String date,
            @RequestParam("message") String message,
            @RequestBody Data data
    ){
        String netId = authManager.getNetId();
        JobNotificationData notificationData = JobNotificationData.createJobNotification(state, date, message);
        notificationService.addJobNotification(netId, notificationData);
        System.out.println("succesfully created a jobNotification");
        System.out.println(data.toString());
        return ResponseEntity.ok("created jobNotification succes");
    }

    @GetMapping("/getJobNotification")
    public ResponseEntity<String> sendJobNotifications(){
        String netId = authManager.getNetId();
        List<JobNotificationData> l = notificationService.getJobNotifications(netId);

        try{
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(l);
            return ResponseEntity.ok(json);
        }catch (Exception e){
            System.out.println("error cresting job notifcation json");
            System.out.println(e);
        }
        return ResponseEntity.ok("not oke");

    }





}
