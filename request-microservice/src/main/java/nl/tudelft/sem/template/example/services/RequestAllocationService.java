package nl.tudelft.sem.template.example.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import nl.tudelft.sem.template.example.domain.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RequestAllocationService {

    private final RestTemplate restTemplate;
    private final RequestRepository requestRepository;

    @Autowired
    public RequestAllocationService(RestTemplateBuilder restTemplateBuilder, RequestRepository requestRepository) {
        this.restTemplate = restTemplateBuilder.build();
        this.requestRepository = requestRepository;
    }



    public List<String> getFacultyUserFaculties(String token) {

        try{
            String url = "https://localhost:8081/getUserFaculties";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);


            ResponseEntity<String> result = restTemplate.postForEntity(url, token, String.class);

            String string = result.getBody();

            assert string != null;
            if(string.equals("")){
                return new ArrayList<>();
            }

            return Arrays.stream(string.split(", ")).collect(Collectors.toList());
        }
        catch (Exception e){
            System.out.println("error with post" + e);
        }
        return new ArrayList<>();

    }

    public List<Resource> getReservedResource(String facultyName, Date preferredDate){

        try{
            String url = "https://localhost:8085/resources/"+ preferredDate.toString() + "/" + facultyName;

            ResponseEntity<AvailableResources> result = restTemplate.getForEntity(url, AvailableResources.class);
            return Objects.requireNonNull(result.getBody()).getResourceList();

        }
        catch (Exception e){
            System.out.println("error with post" + e);
        }

        return new ArrayList<>();

    }

    public boolean enoughResourcesForJob(Request request){

        List<Resource> resources = getReservedResource(request.getFaculty(),request.getPreferredDate());
        boolean flag = false;

        for (int i = 0; i < resources.size(); i++) {
            Resource currentResource = resources.get(i);

            if(currentResource.getResourceCPU() >= request.getCpu() &&
                    currentResource.getResourceGPU() >= request.getGpu() &&
                    currentResource.getResourceMemory() >= request.getMemory()){
                return true;
            }

        }
        return false;

    }


    public void sendRequestToCluster(Request request) throws JsonProcessingException {

        try{
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
        }
     catch (Exception e){
        System.out.println("error with post" + e);
     }


    }

    public void sendDeclinedRequestToUserService(Request request) {


        try{
            String url = "https://localhost:8081/notification";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            //List<String> enumValues = Arrays.asList(Arrays.toString(AppUser.Faculty.values()));



            JSONObject json = new JSONObject();
            json.put("date",request.getPreferredDate());
            json.put("type","REQUEST");
            json.put("state","REJECTED");
            json.put("message",request.getDescription());
            json.put("netId",request.getNetId());


            ResponseEntity<String> result = restTemplate.postForEntity(url, json, String.class);
            if (result.getBody().equals("ok")) {
                return;
            }
        }
        catch (Exception e){
            System.out.println("error with post" + e);
        }


    }

    public boolean verifyUser(String netId, String token, String faculty) {

        String url = "to be decided";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        VerificationDTO dto = new VerificationDTO(netId,token,faculty);


        ResponseEntity<Boolean> result = restTemplate.postForEntity(url, dto, Boolean.class);

        return Boolean.TRUE.equals(result.getBody());

    }
}
