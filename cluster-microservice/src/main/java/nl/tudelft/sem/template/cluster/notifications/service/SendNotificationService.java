package nl.tudelft.sem.template.cluster.notifications.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import nl.tudelft.sem.template.cluster.notifications.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SendNotificationService {

    private final RestTemplate restTemplate;

    /**
     * Instantiates a new sendNotificationService.
     *
     * @param restTemplateBuilder the rest template builder
     */
    @Autowired
    public SendNotificationService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
     * Send a notification to the endpoint receiveJobNotification in the NotificationController
     * in the authentication microservice.
     *
     * @param notification the notification to be sent
     * @throws JsonProcessingException when the notification could not be sent
     */
    public void sendNotificationToUser(Notification notification) throws JsonProcessingException {
        try {
            String url = "http://localhost:8081/notification";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(notification);
            ResponseEntity<String> result = restTemplate.postForEntity(url, json, String.class);
            /*if (result.getBody().equals("ok")) {
                return;
            }*/
        } catch (Exception e) {
            System.out.println("error with post: " + e);
        }
    }

}
