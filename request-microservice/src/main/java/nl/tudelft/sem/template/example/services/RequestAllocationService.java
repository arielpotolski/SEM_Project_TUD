package nl.tudelft.sem.template.example.services;

import nl.tudelft.sem.template.example.domain.Request;
import nl.tudelft.sem.template.example.domain.Resource;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RequestAllocationService {

    private final RestTemplate restTemplate;

    public RequestAllocationService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
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
}
