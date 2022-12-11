package nl.tudelft.sem.template.authentication.startup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.tomcat.jni.Time;
import org.springframework.beans.factory.annotation.Autowired;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class runner implements ApplicationListener<ContextRefreshedEvent> {


    public static int counter;
    private final RestTemplate restTemplate;

    @Autowired
    public runner(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }


    @Override public void onApplicationEvent(ContextRefreshedEvent event) {
        sendFaculties();
    }

    private void sendFaculties() {
            Thread t = new Thread(() -> {
                while(true) {
                    try {
                        Thread.sleep(1000);


                        String url = "https://localhost:8085/faculties";
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        List<String> enumValues = Arrays.asList(Arrays.toString(AppUser.Faculty.values()));

                        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                        String json = ow.writeValueAsString(enumValues);
                        System.out.println(json);
                        ResponseEntity<String> result = restTemplate.postForEntity(url, json, String.class);
                        if (result.getBody().equals("ok")) {
                            return;
                        }
                    } catch (Exception e){
                        System.out.println("error with post" + e);
                    }
                }});

            try{
                t.start();
            }catch (Exception e){
                System.out.println("error with thread" + e);
            }

    }


}
