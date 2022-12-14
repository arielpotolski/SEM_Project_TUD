package nl.tudelft.sem.template.authentication.startup;

import java.util.Arrays;
import java.util.List;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class Runner implements ApplicationListener<ContextRefreshedEvent> {

    private final transient RestTemplate restTemplate;

    @Autowired
    public Runner(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }


    @Override public void onApplicationEvent(ContextRefreshedEvent event) {
        sendFaculties();
    }

    private void sendFaculties() {

        Thread thread = new Thread(() -> {

            HttpStatus status = HttpStatus.BAD_REQUEST;
            while (status != HttpStatus.OK) {
                try {
                    Thread.sleep(1000);

                    String url = "http://localhost:8082/faculties";
                    List<String> enumValues = List.of(Arrays.toString(AppUser.Faculty.values()));

                    ResponseEntity<String> result = restTemplate.postForEntity(url, enumValues, String.class);
                    status = result.getStatusCode();
                    if ((result.getStatusCode() == HttpStatus.OK)) {
                        System.out.println("Cluster service found");
                        return;
                    } else {
                        System.out.println(result.getStatusCode());
                    }
                } catch (Exception e) {
                    System.out.println("Cluster service not yet online" + e.getMessage());
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
