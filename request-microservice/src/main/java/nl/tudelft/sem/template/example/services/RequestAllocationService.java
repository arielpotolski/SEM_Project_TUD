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
import java.util.Date;
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

    public List<Resource> getReservedResource(String facultyName, Date preferredDate){


        // Needs to be confirmed by Cluster team
        String url = "https://localhost:8085/resources/" + facultyName + "/" + preferredDate.toString();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request =
                new HttpEntity<String>(facultyName, headers);

        Resource resourceRet =
                restTemplate.postForObject(url, request, Resource.class);

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


    public void sendRequestToCluster(Request request) {

        String url = "https://localhost:8085/cluster/request";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> result = restTemplate.postForEntity(url, request, String.class);

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
