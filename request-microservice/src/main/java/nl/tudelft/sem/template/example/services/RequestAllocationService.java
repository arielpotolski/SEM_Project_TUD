package nl.tudelft.sem.template.example.services;

import nl.tudelft.sem.template.example.domain.Request;
import nl.tudelft.sem.template.example.domain.Resource;
import nl.tudelft.sem.template.example.domain.RequestRepository;
import nl.tudelft.sem.template.example.domain.VerificationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

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

        return new ArrayList<>();
        // To be impl
//        String url = "to be decided";
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.add("token", token);
//
//
//
//        ResponseEntity<List<String>> response = restTemplate.postForEntity(url, entity, String.class);




    }

    public Resource getReservedResource(String facultyName){


        // Needs to be confirmed by Cluster team
        String url = "https://localhost:8082/cluster/facultyResource";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request =
                new HttpEntity<String>(facultyName, headers);

        Resource resourceRet =
                restTemplate.postForObject(url, request, Resource.class);

        return resourceRet;

    }

    public boolean enoughResourcesForJob(Request request){

        Resource currentResource = getReservedResource(request.getFaculty());

        if(currentResource.getResourceCPU() < request.getCpu()){
            return false;
        }
        if(currentResource.getResourceGPU() < request.getGpu()){
            return false;
        }
        return currentResource.getResourceMemory() >= request.getMemory();

    }


    public void sendRequestToCluster(Request request) {

        String url = "https://localhost:8082/cluster/sendVerifiedRequest";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Request> result = restTemplate.postForEntity(url, request, Request.class);

    }

    public void sendDeclinedRequestToUserService(Request request) {

        // URL to be determined
        String url = "to be decided";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Request> result = restTemplate.postForEntity(url, request, Request.class);

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
