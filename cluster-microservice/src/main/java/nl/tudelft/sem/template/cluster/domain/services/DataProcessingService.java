package nl.tudelft.sem.template.cluster.domain.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.cluster.models.GetFacultyResponseModel;
import nl.tudelft.sem.template.cluster.models.TokenRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DataProcessingService {

    private final RestTemplate restTemplate;

    @Autowired
    public DataProcessingService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    // move to new service?
    public List<String> getFacultiesOfGivenUser(String userToken) {
        try {
            String url = "http://localhost:8081/getUserFaculties";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(userToken);

            HttpEntity<TokenRequestModel> entity = new HttpEntity<>(new TokenRequestModel(userToken), headers);
            ResponseEntity<GetFacultyResponseModel> result = restTemplate
                    .postForEntity(url, entity, GetFacultyResponseModel.class);

            String faculties = result.getBody().getFaculties();

            assert faculties != null;
            faculties = faculties.replace("[", "").replace("]", "");
            if (faculties.equals("")) {
                return new ArrayList<>();
            }

            return Arrays.stream(faculties.split(", ")).collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println("Could not acquire faculties associated with user.");
        }
        return new ArrayList<>();
    }

    // get all existing faculties, check if in DB - remove if no
    // for each faculty, calculate available resources for all days until last day in schedule + 1
    // add methods to filter result, e.g., return for all faculties for all days, for given day for all faculties, and
    // for given faculty for given day

}
