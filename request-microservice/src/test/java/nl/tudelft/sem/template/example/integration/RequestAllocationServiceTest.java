package nl.tudelft.sem.template.example.integration;

import nl.tudelft.sem.template.example.authentication.AuthManager;
import nl.tudelft.sem.template.example.authentication.JwtTokenVerifier;
import nl.tudelft.sem.template.example.controllers.JobRequestController;
import nl.tudelft.sem.template.example.domain.Request;
import nl.tudelft.sem.template.example.domain.Resource;
import nl.tudelft.sem.template.example.integration.utils.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.text.SimpleDateFormat;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@ExtendWith(SpringExtension.class)
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockTokenVerifier", "mockAuthenticationManager"})
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)

@AutoConfigureMockMvc
public class RequestAllocationServiceTest {

    @MockBean
    private JobRequestController jobRequestController;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private transient JwtTokenVerifier mockJwtTokenVerifier;

    @Autowired
    private transient AuthManager mockAuthenticationManager;

    @BeforeEach
    public void setup() {

        when(mockAuthenticationManager.getNetId()).thenReturn("Alexander");
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken(anyString())).thenReturn("Alexander");

    }

    @Test
    public void getFacultyUserFacultiesTest() {

    }

    @Test
    public void getReservedResourceTest(){



    }

//    @Test
//    public void enoughResourcesForJobTest() throws Exception{
//
//        String dateString = "2023-12-12";
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
//
//        Request request = new Request(123l, "Test", "Test", "Test", "AE",
//                2.0, 1.0, 1.0, true, simpleDateFormat.parse(dateString));
//
//
//        Resource res1 = new Resource("AE", 23.0, 23.0, 23.0);
//        Resource res2 = new Resource("AE", 23.0, 23.0, 23.0);
//
//        List<Resource> resources = List.of(res1, res2);
//
//
//    }

    @Test
    public void sendRequestToClusterTest(){

    }

    @Test
    public void sendDeclinedRequestToUserService() throws Exception{



    }


}
