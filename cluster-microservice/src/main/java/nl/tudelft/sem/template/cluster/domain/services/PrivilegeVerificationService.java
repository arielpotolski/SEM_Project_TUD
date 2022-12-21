package nl.tudelft.sem.template.cluster.domain.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.cluster.authentication.AuthManager;
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

/**
 * The second layer of authorization. Verifies whether a user is allowed to make the request they are making.
 */
@Service
public class PrivilegeVerificationService {

    private final transient AuthManager authManager;
    private final transient RestTemplate restTemplate;

    @Autowired
    public PrivilegeVerificationService(AuthManager authManager, RestTemplateBuilder restTemplateBuilder) {
        this.authManager = authManager;
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
     * Contacts the user service to acquire all faculties assigned to a given user.
     *
     * @param userToken the authorization token of the user.
     *
     * @return the list of all faculties assigned to the user with the given token.
     */
    private List<String> getFacultiesOfGivenUser(String userToken) {
        try {
            String url = "http://localhost:8081/getUserFaculties";
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
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

    /**
     * Verifies whether a user has admin privileges, and if not, whether the faculty they are requesting access to is
     * assigned to them.
     *
     * @param requestHeaders the headers of the HTTP request sent by the user.
     * @param requestedFaculty the faculty that the user requested access to.
     *
     * @return whether the user is allowed to make the request.
     */
    public boolean verifyAccountOfCorrectFaculty(HttpHeaders requestHeaders, String requestedFaculty) {
        var userToken = requestHeaders.get("authorization").get(0).replace("Bearer ", "");
        var role = this.authManager.getRole();
        var admin = "SYSADMIN";
        boolean adminPermissions = admin.equals(role);

        return adminPermissions || (requestedFaculty != null && this.getFacultiesOfGivenUser(userToken)
                .contains(requestedFaculty));
    }

}
