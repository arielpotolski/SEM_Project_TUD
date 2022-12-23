package nl.tudelft.sem.template.cluster.domain.services;

import lombok.Getter;
import lombok.Setter;
import nl.tudelft.sem.template.cluster.models.NotificationRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Getter
@Setter
public class NotificationManagerService {

    private RestTemplate restTemplate;

    /**
     * Initializes the class.
     *
     * @param restTemplateBuilder the restTemplateBuilder
     */
    @Autowired
    public NotificationManagerService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
     * This method sends a notification to the user microservice 'notification'
     * endpoint.
     *
     * @param model the notification in the correct model
     * @param token the token
     * @return whether the notification was sent correctly or not
     */
    public boolean sendNotification(NotificationRequestModel model, String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            String url = "http://localhost:8081/notification";

            HttpEntity<NotificationRequestModel> entity = new HttpEntity<>(model, headers);
            ResponseEntity<String> result = restTemplate.postForEntity(url, entity, String.class);
            if (result.getBody().equals("ok")) {
                return true;
            }
        } catch (Exception e) {
            System.out.println("error with post" + e);
            return false;
        }
        return true;
    }
}
