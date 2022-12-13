package nl.tudelft.sem.template.example.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.example.domain.AvailableResources;
import nl.tudelft.sem.template.example.domain.Request;
import nl.tudelft.sem.template.example.domain.RequestRepository;
import nl.tudelft.sem.template.example.domain.Resource;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * The Request service initiates communication with other microservices and exchanges information.
 */
@Service
public class RequestAllocationService {

    private final RestTemplate restTemplate;
    private final RequestRepository requestRepository;

    /**
     * Instantiates a new Request allocation service.
     *
     * @param restTemplateBuilder the rest template builder
     * @param requestRepository   the request repository
     */
    @Autowired
    public RequestAllocationService(RestTemplateBuilder restTemplateBuilder, RequestRepository requestRepository) {
        this.restTemplate = restTemplateBuilder.build();
        this.requestRepository = requestRepository;
    }


    /**
     * This method is responsible for getting the associated faculties with the user who made the request.
     * It receives that info through an endpoint from the user service
     *
     * @param token the token
     * @return the faculty user faculties
     */
    public List<String> getFacultyUserFaculties(String token) {

        try {
            String url = "https://localhost:8081/getUserFaculties";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);


            ResponseEntity<String> result = restTemplate.postForEntity(url, token, String.class);

            String string = result.getBody();

            assert string != null;
            if (string.equals("")) {
                return new ArrayList<>();
            }

            return Arrays.stream(string.split(", ")).collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("error with post" + e);
        }
        return new ArrayList<>();

    }

    /**
     * This method is responsible for getting the available resources for a particular faculty up until a given date.
     * If a job can be executed prior to that date, it will be done earlier.
     *
     * @param facultyName   the faculty name
     * @param preferredDate the preferred date
     * @return the list
     */
    public List<Resource> getReservedResource(String facultyName, Date preferredDate) {

        try {
            String url = "https://localhost:8085/resources/" + preferredDate.toString() + "/" + facultyName;

            ResponseEntity<AvailableResources> result = restTemplate.getForEntity(url, AvailableResources.class);
            return Objects.requireNonNull(result.getBody()).getResourceList();

        } catch (Exception e) {
            System.out.println("error with post" + e);
        }

        return new ArrayList<>();

    }

    /**
     * Checks if there are enough computational resources for a given job to be executed.
     *
     * @param request the request
     * @return the boolean
     */
    public boolean enoughResourcesForJob(Request request) {

        List<Resource> resources = getReservedResource(request.getFaculty(), request.getPreferredDate());
        boolean flag = false;

        for (int i = 0; i < resources.size(); i++) {
            Resource currentResource = resources.get(i);

            if (currentResource.getResourceCpu() >= request.getCpu()
                    &&
                    currentResource.getResourceGpu() >= request.getGpu()
                    &&
                    currentResource.getResourceMemory() >= request.getMemory()) {
                return true;
            }

        }
        return false;

    }


    /**
     * If the user is verified and there are enough resources available, the job request is forwarded to the cluster service.
     *
     * @param request the request
     * @throws JsonProcessingException the json processing exception
     */
    public void sendRequestToCluster(Request request) throws JsonProcessingException {

        try {
            String url = "https://localhost:8085/cluster/request";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            //List<String> enumValues = Arrays.asList(Arrays.toString(AppUser.Faculty.values()));

            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(request);
            System.out.println(json);
            ResponseEntity<String> result = restTemplate.postForEntity(url, json, String.class);
            if (result.getBody().equals("ok")) {
                return;
            }
        } catch (Exception e) {
            System.out.println("error with post" + e);
        }


    }

    /**
     * This method is responsible for notifying the user if their job request is declined for some reason.
     * It sends the job notification to the user service.
     *
     * @param request the request
     */
    public void sendDeclinedRequestToUserService(Request request) {


        try {
            String url = "https://localhost:8081/notification";

            JSONObject json = new JSONObject();
            json.put("date", request.getPreferredDate());
            json.put("type", "REQUEST");
            json.put("state", "REJECTED");
            json.put("message", request.getDescription());
            json.put("netId", request.getNetId());


            ResponseEntity<String> result = restTemplate.postForEntity(url, json, String.class);
            if (result.getBody().equals("ok")) {
                return;
            }
        } catch (Exception e) {
            System.out.println("error with post" + e);
        }


    }

}
