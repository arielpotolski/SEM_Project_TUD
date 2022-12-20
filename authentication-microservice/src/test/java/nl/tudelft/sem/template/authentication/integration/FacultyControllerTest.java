package nl.tudelft.sem.template.authentication.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@ExtendWith(SpringExtension.class)
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockPasswordEncoder", "mockTokenGenerator", "mockAuthenticationManager", "mockTokenVerifier"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class FacultyControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGettingAllFaculties() throws Exception {

        ResultActions resultActions = mockMvc.perform(get("/getAllFaculties"));
        MvcResult result = resultActions
                .andExpect(status().isOk())
                .andReturn();
        assertThat(result.getResponse().getContentAsString()).isEqualTo("[\"[CIVIL, IO, EWI]\"]");
    }
}
