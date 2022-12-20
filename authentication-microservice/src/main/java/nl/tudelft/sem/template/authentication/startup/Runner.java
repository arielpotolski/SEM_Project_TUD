package nl.tudelft.sem.template.authentication.startup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class Runner implements ApplicationListener<ContextRefreshedEvent> {

    private final transient RestTemplate restTemplate;

    private transient Thread thread;

    @Autowired
    public Runner(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }


    @Override public void onApplicationEvent(ContextRefreshedEvent event) {
        sendFaculties();
    }

    private void sendFaculties() {

        thread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);

                    String url = "https://localhost:8082/faculties";
                    List<String> enumValues = List.of(Arrays.toString(AppUser.Faculty.values()));

                    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                    String json = ow.writeValueAsString(enumValues);

                    ResponseEntity<String> result = restTemplate.postForEntity(url, json, String.class);
                    if (Objects.equals(result.getBody(), "ok")) {
                        return;
                    }
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
