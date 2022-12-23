package nl.tudelft.sem.template.cluster.domain.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import nl.tudelft.sem.template.cluster.domain.providers.TimeProvider;
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
    private TimeProvider timeProvider;

    /**
     * Initializes the class.
     *
     * @param restTemplateBuilder the restTemplateBuilder
     */
    @Autowired
    public NotificationManagerService(RestTemplateBuilder restTemplateBuilder, TimeProvider timeProvider) {
        this.restTemplate = restTemplateBuilder.build();
        this.timeProvider = timeProvider;
    }

    /**
     * This method sends a notification to the user microservice 'notification'
     * endpoint.
     *
     * @param model the notification in the correct model
     *
     * @return whether the notification was sent correctly or not
     */
    public boolean sendNotification(NotificationRequestModel model) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String token = Jwts.builder().setSubject("SYSTEM")
                    .claim("roles", "ROLE_SYSTEM")
                    .setIssuedAt(new Date(timeProvider.getCurrentTime().toEpochMilli()))
                    .setExpiration(new Date(timeProvider.getCurrentTime().toEpochMilli() + 24 * 60 * 60 * 1000))
                    .signWith(SignatureAlgorithm.HS512, "exampleSecret").compact();
            headers.setBearerAuth(token);

            String url = "http://localhost:8081/notification";

            HttpEntity<NotificationRequestModel> entity = new HttpEntity<>(model, headers);
            ResponseEntity<String> result = restTemplate.postForEntity(url, entity, String.class);
            if (result.getStatusCode().value() == 200) {
                return true;
            }
        } catch (Exception e) {
            System.out.println("error with post" + e);
            return false;
        }
        return true;
    }
}
