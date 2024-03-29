package nl.tudelft.sem.template.example.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import nl.tudelft.sem.template.example.TokenRequestModel;
import nl.tudelft.sem.template.example.domain.AvailableResources;
import nl.tudelft.sem.template.example.domain.FacultiesResponseModel;
import nl.tudelft.sem.template.example.domain.JobRequestRequestModel;
import nl.tudelft.sem.template.example.domain.NotificationRequestModel;
import nl.tudelft.sem.template.example.domain.Request;
import nl.tudelft.sem.template.example.domain.RequestRepository;
import nl.tudelft.sem.template.example.domain.Resource;
import nl.tudelft.sem.template.example.domain.ResourceResponseModel;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * The Request service initiates communication with other microservices and exchanges information.
 */
@Service
@Getter
public class RequestAllocationService {

    private RestTemplate restTemplate;
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

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
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
            String url = "http://localhost:8081/getUserFaculties";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<TokenRequestModel> entity = new HttpEntity<>(new TokenRequestModel(token), headers);
            ResponseEntity<FacultiesResponseModel> result = restTemplate
                    .postForEntity(url, entity, FacultiesResponseModel.class);

            String string = result.getBody().getFaculties()
                    .replace("[", "").replace("]", "");

            if (string.equals("")) {
                return Collections.emptyList();
            }

            return Arrays.stream(string.split(", ")).collect(Collectors.toList());

        } catch (Exception e) {
            System.out.println("error with post:" + e);
        }
        return Collections.emptyList();

    }

    /**
     * This method is responsible for getting the available resources for a particular faculty up until a given date.
     * If a job can be executed prior to that date, it will be done earlier.
     *
     * @param facultyName   the faculty name
     * @param preferredDate the preferred date
     * @return the list
     */
    public List<Resource> getReservedResource(String facultyName, LocalDate preferredDate, String token) {
        try {
            String url = "http://localhost:8082/resources/availableUntil/"
                    + preferredDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    + "/" + facultyName;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<TokenRequestModel> entity = new HttpEntity<>(new TokenRequestModel(token), headers);
            var result = restTemplate.exchange(url, HttpMethod.GET, entity, ResourceResponseModel[].class);
            var listOfResources = Stream.of(Objects.requireNonNull(result.getBody()))
                    .map(x -> new Resource(x.getFacultyName(), x.getResourceCpu(),
                            x.getResourceGpu(), x.getResourceMemory())).collect(Collectors.toList());

            return listOfResources;
        } catch (Exception e) {
            System.out.println("Error getting reserved resources: " + e.getMessage());
            return Collections.emptyList();
        }
    }


    /**
     * Checks if there are enough computational resources for a given job to be executed.
     *
     * @param request the request
     * @return the boolean
     */
    public boolean enoughResourcesForJob(Request request, String token) {

        List<Resource> resources = getReservedResource(request.getFaculty(), request.getPreferredDate(), token);

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
     */
    public boolean sendRequestToCluster(Request request, String token) {

        try {
            String url = "http://localhost:8082/request";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            HttpEntity<JobRequestRequestModel> entity =
                    new HttpEntity<>(JobRequestRequestModel.convertToRequestModel(request), headers);
            ResponseEntity<String> result = restTemplate.postForEntity(url, entity, String.class);
            if (result.getBody().equals("ok")) {
                return true;
            }
        } catch (Exception e) {
            System.out.println("error with post: " + e);
            return false;
        }
        return true;


    }

    /**
     * This method is responsible for notifying the user if their job request is declined for some reason.
     * It sends the job notification to the user service.
     *
     * @param request the request
     */
    public boolean sendDeclinedRequestToUserService(Request request, String token) {


        try {

            var model = new NotificationRequestModel(
                    request.getPreferredDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    "REQUEST", "REJECTED",
                    request.getDescription(), request.getNetId());

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
            System.out.println("error with post: " + e);
            return false;
        }
        return false;


    }

}
