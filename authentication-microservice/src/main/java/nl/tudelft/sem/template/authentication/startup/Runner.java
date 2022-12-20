package nl.tudelft.sem.template.authentication.startup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import nl.tudelft.sem.template.authentication.authentication.JwtTokenGenerator;
import nl.tudelft.sem.template.authentication.authentication.JwtUserDetailsService;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.NetId;
import nl.tudelft.sem.template.authentication.domain.user.Password;
import nl.tudelft.sem.template.authentication.domain.user.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class Runner implements ApplicationListener<ContextRefreshedEvent> {

    private final transient RestTemplate restTemplate;
    private final transient RegistrationService registrationService;
    private final transient JwtTokenGenerator jwtTokenGenerator;
    private final transient JwtUserDetailsService jwtUserDetailsService;

    /**
     * Constructor of the class.
     *
     * @param restTemplateBuilder object that makes the request
     * @param registrationService service which can register a user
     * @param jwtTokenGenerator service that generates the token
     * @param jwtUserDetailsService service that generates the user details
     */
    @Autowired
    public Runner(RestTemplateBuilder restTemplateBuilder, RegistrationService registrationService,
                  JwtTokenGenerator jwtTokenGenerator, JwtUserDetailsService jwtUserDetailsService) {
        this.restTemplate = restTemplateBuilder.build();
        this.registrationService = registrationService;
        this.jwtTokenGenerator = jwtTokenGenerator;
        this.jwtUserDetailsService = jwtUserDetailsService;
    }

    @Override public void onApplicationEvent(ContextRefreshedEvent event) {
        sendFaculties();
    }

    private void sendFaculties() {

        Thread thread = new Thread(() -> {
            while (true) {
                HttpURLConnection connection;
                try {
                    URL u = new URL("http://localhost:8082");
                    connection = (HttpURLConnection) u.openConnection();
                    connection.setRequestMethod("HEAD");
                    int code = connection.getResponseCode();
                    if (code == 200 || code == 401) {
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("error" + e.getMessage());
                }
            }
            System.out.println("Successfully polled Cluster Microservice");
            try {
                registrationService.registerUser(new NetId("user"), new Password("pass"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            String token = jwtTokenGenerator.generateToken(jwtUserDetailsService.loadUserByUsername("user"));

            HttpStatus status = HttpStatus.BAD_REQUEST;
            while (status != HttpStatus.OK) {
                try {
                    Thread.sleep(1000);
                    String url = "http://localhost:8082/faculties";
                    List<String> enumValues = List.of(Arrays.toString(AppUser.Faculty.values()));
                    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                    String json = ow.writeValueAsString(enumValues);

                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.setBearerAuth(token);
                    HttpEntity<String> entity = new HttpEntity<>(json, headers);

                    ResponseEntity<String> result = restTemplate.postForEntity(url, entity, String.class);
                    status = result.getStatusCode();
                } catch (Exception e) {
                    System.out.println("error with reaching Cluster Microservice. Trying again \n" + e.getMessage());
                }
            }
            System.out.println("Successfully sent all faculties to Cluster Microservice");
        });
        thread.start();
    }


}
