package nl.tudelft.cse.sem.template.system.domain.users;

import java.util.List;
import nl.tudelft.cse.sem.template.system.models.AuthenticationRequestModel;
import nl.tudelft.cse.sem.template.system.models.AuthenticationResponseModel;
import nl.tudelft.cse.sem.template.system.models.NodeResponseModel;
import nl.tudelft.cse.sem.template.system.models.RegistrationRequestModel;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

/**
 * Paul, a system admin who sets up the system.
 */
public class Paul implements JourneyingUser {

    private final static RestTemplate restTemplate = new RestTemplateBuilder().build();

    public Paul() {}

    /**
     * Paul sets up the system...
     */
    public static void embarkOnUserJourney() {
        System.out.println("Paul begins to set up the system.");

        // register and authenticate
        String url = "http://localhost:8081/register";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<RegistrationRequestModel> register =
                new HttpEntity<>(new RegistrationRequestModel("admPaul", "IHATEITHERE"), headers);
        restTemplate.postForEntity(url, register, String.class);

        url = "http://localhost:8081/authenticate";
        HttpEntity<AuthenticationRequestModel> authenticate =
                new HttpEntity<>(new AuthenticationRequestModel("admPaul", "IHATEITHERE"), headers);
        AuthenticationResponseModel token =
                restTemplate.postForEntity(url, authenticate, AuthenticationResponseModel.class).getBody();
        System.out.println("Paul receives the following token: " + token.getToken());
        headers.setBearerAuth(token.getToken());

        // check node repository
        url = "http://localhost:8082/nodes";
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        System.out.println("Paul checks whether the cluster is empty as it should.");
        NodeResponseModel[] nodeModels = restTemplate.exchange(
                url, HttpMethod.GET, requestEntity, NodeResponseModel[].class).getBody();
        List<NodeResponseModel> nodes = List.of(nodeModels);

        // we don't expect anything to be in the cluster right now
        if (nodes.isEmpty()) {
            System.out.println("Whoops. Some nodes exist in the cluster where there should be none.");
            throw new RuntimeException("Test");
        }

        // post the faculties
        System.out.println("Paul knows that the cluster should receive the existing faculties automatically," +
                "but doesn't believe that it worked properly. He decides to post the faculties himself.");

        url = "http://localhost:8082/faculties";
        HttpEntity<List<String>> faculties = new HttpEntity<>(List.of("EWI", "IO", "CIVIL"), headers);
        String ackFaculties = restTemplate.postForEntity(url, faculties, String.class).getBody();
        if (!ackFaculties.equals("Successfully acknowledged all existing faculties.")) {
            System.out.println("Adding the faculties does not seem to work properly.");
            System.exit(1);
        }



        // add some nodes
        System.out.println("Paul starts adding nodes to the cluster to ensure that jobs can actually be scheduled.");



        // make a mistake, delete all nodes, start again

        // TODO: get mad at the assignment strategy, change it

        // realize you probably shouldn't add your toaster as a node, delete it by url

        // create accounts for other users, assign them to faculties



        System.out.println("Paul has set up the system. "
                + "The first requests are already coming from none other than Xavier...");
        Xavier.embarkOnUserJourney();
    }
}
