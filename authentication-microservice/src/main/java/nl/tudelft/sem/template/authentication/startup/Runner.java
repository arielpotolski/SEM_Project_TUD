package nl.tudelft.sem.template.authentication.startup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import nl.tudelft.sem.template.authentication.domain.providers.TimeProvider;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class Runner implements ApplicationListener<ContextRefreshedEvent> {

    private final transient RestTemplate restTemplate;
    private final transient TimeProvider timeProvider;

    private transient Thread thread;

    @Autowired
    public Runner(RestTemplateBuilder restTemplateBuilder, TimeProvider timeProvider) {
        this.restTemplate = restTemplateBuilder.build();
        this.timeProvider = timeProvider;
    }


    @Override public void onApplicationEvent(ContextRefreshedEvent event) {
        sendFaculties();
    }

    private void sendFaculties() {

        thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);

                    String url = "http://localhost:8082/faculties";
                    List<String> enumValues = Stream.of(AppUser.Faculty.values())
                            .map(Enum::toString).collect(Collectors.toList());

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    String token = Jwts.builder().setSubject("SYSTEM")
                            .claim("roles", "ROLE_SYSTEM")
                            .setIssuedAt(new Date(timeProvider.getCurrentTime().toEpochMilli()))
                            .setExpiration(new Date(timeProvider.getCurrentTime().toEpochMilli() + 24 * 60 * 60 * 1000))
                            .signWith(SignatureAlgorithm.HS512, "exampleSecret").compact();
                    headers.setBearerAuth(token);

                    HttpEntity<List<String>> entity = new HttpEntity<>(enumValues, headers);

                    ResponseEntity<String> result = restTemplate.postForEntity(url, entity, String.class);
                    if (Objects.equals(result.getStatusCode().value(), 200)) {
                        return;
                    }
                } catch (org.springframework.web.client.HttpClientErrorException e) {
                    return;
                } catch (Exception e) {
                    System.out.println("Cluster service not yet online");
                }
            }
        });
        try {
            thread.start();
        } catch (Exception e) {
            System.out.println("Error with thread that talks to cluster: " + e);
        }
    }


}
