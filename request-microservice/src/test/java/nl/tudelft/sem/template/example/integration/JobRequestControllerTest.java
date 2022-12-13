package nl.tudelft.sem.template.example.integration;

import java.text.SimpleDateFormat;
import nl.tudelft.sem.template.example.authentication.AuthManager;
import nl.tudelft.sem.template.example.authentication.JwtTokenVerifier;
import nl.tudelft.sem.template.example.domain.Request;
import nl.tudelft.sem.template.example.domain.RequestRepository;
import nl.tudelft.sem.template.example.integration.utils.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockTokenVerifier", "mockAuthenticationManager"})
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)

@AutoConfigureMockMvc
public class JobRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private transient JwtTokenVerifier mockJwtTokenVerifier;

    @Autowired
    private transient AuthManager mockAuthenticationManager;

    @Autowired
    private transient RequestRepository requestRepository;

    // No need for specific token for testing
    @BeforeEach
    public void setup() {

        when(mockAuthenticationManager.getNetId()).thenReturn("Alexander");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("Alexander");

    }


    @Test
    public void sendRequestTestInFacultyNull () throws Exception {

        Request request = new Request();

        ResultActions result = mockMvc.perform(post("/job/sendRequest")
                .accept(MediaType.APPLICATION_JSON).content(JsonUtil.serialize(request))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));


        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo("You are not verified to send requests to this faculty");

    }

    @Test
    public void sendRequestTestNotInFaculty () throws Exception {

        String dateString = "2023-12-12";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Request request = new Request(123L, "Test", "Test", "Test", "AE",
                2.0, 1.0, 1.0, true, simpleDateFormat.parse(dateString));

        ResultActions result = mockMvc.perform(post("/job/sendRequest")
                .accept(MediaType.APPLICATION_JSON).content(JsonUtil.serialize(request))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        assertThat(response).isEqualTo("You are not verified to send requests to this faculty");

    }

    //TODO: Cannot mock token properly, so we need a different test

//    @Test
//    public void sendRequestTestInFaculty () throws Exception {
//
//        String dateString = "2023-12-12";
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
//
//        Request request = new Request(123l, "Test", "Test", "Test", "AE",
//                2.0, 1.0, 1.0, false, simpleDateFormat.parse(dateString));
//
//        ResultActions result = mockMvc.perform(post("/job/sendRequest")
//                .accept(MediaType.APPLICATION_JSON).content(JsonUtil.serialize(request))
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer MockedToken"));
//
//        result.andExpect(status().isOk());
//        String response = result.andReturn().getResponse().getContentAsString();
//        assertThat(response).isEqualTo("The request was sent. Now it is to be approved by faculty.");
//
//    }


    // Can also add an assert if we change the request in the controller

    @Test
    public void pendingRequestsTest () throws Exception {

        ResultActions result = mockMvc.perform(get("/job/pendingRequests")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken"));

        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        //assertThat(response).isEqualTo("You are not verified to send requests to this faculty");

    }


    @Test
    public void sendApprovalsTest () {
    }



}

