package nl.tudelft.sem.template.authentication.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import nl.tudelft.sem.template.authentication.authentication.JwtTokenGenerator;
import nl.tudelft.sem.template.authentication.authentication.JwtTokenVerifier;
import nl.tudelft.sem.template.authentication.authtemp.AuthManager;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.NetId;
import nl.tudelft.sem.template.authentication.domain.user.Password;
import nl.tudelft.sem.template.authentication.domain.user.Role;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.integration.utils.JsonUtil;
import nl.tudelft.sem.template.authentication.models.ApplyFacultyRequestModel;
import nl.tudelft.sem.template.authentication.models.AuthenticationRequestModel;
import nl.tudelft.sem.template.authentication.models.AuthenticationResponseModel;
import nl.tudelft.sem.template.authentication.models.GetFacultyRequestModel;
import nl.tudelft.sem.template.authentication.models.GetFacultyResponseModel;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModel;
import nl.tudelft.sem.template.authentication.services.PasswordHashingService;
import nl.tudelft.sem.template.authentication.services.RegistrationService;
import nl.tudelft.sem.template.authentication.services.RoleControlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
public class UsersTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private transient PasswordHashingService mockPasswordEncoder;

    @Autowired
    private transient JwtTokenGenerator mockJwtTokenGenerator;

    @Autowired
    private transient AuthenticationManager mockAuthenticationManager;

    @Autowired
    private transient JwtTokenVerifier mockJwtTokenVerifier;

    @Autowired
    private transient UserRepository userRepository;

    @Autowired
    private transient RegistrationService registrationService;

    @Autowired
    private transient RoleControlService roleControlService;

    /**
     * Sets up the tests.
     */
    @BeforeEach
    public void setup() {
        this.roleControlService.save(new Role("USER"));
        this.roleControlService.save(new Role("FACULTY"));
        this.roleControlService.save(new Role("SYSADMIN"));
        this.roleControlService.save(new Role("SYSTEM"));
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(mockJwtTokenVerifier.getNetIdFromToken("MockedToken")).thenReturn("SomeUser");
        when(mockJwtTokenVerifier.getRoleFromToken("MockedToken")).thenReturn("USER");
    }

    @Test
    public void registerAdminTest() throws Exception {
        final NetId testUser = new NetId("admUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        RegistrationRequestModel model = new RegistrationRequestModel();
        model.setNetId(testUser.toString());
        model.setPassword(testPassword.toString());

        // Act
        ResultActions resultActions = mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        resultActions.andExpect(status().isOk());

        AppUser savedUser = userRepository.findByNetId(testUser).orElseThrow();

        assertThat(savedUser.getNetId()).isEqualTo(testUser);
        assertThat(savedUser.getPassword()).isEqualTo(testHashedPassword);
        assertThat(savedUser.getRole()).isEqualTo(new Role("SYSADMIN"));
    }

    @Test
    public void register_withValidData_worksCorrectly() throws Exception {
        // Arrange
        final NetId testUser = new NetId("SomeUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        RegistrationRequestModel model = new RegistrationRequestModel();
        model.setNetId(testUser.toString());
        model.setPassword(testPassword.toString());

        // Act
        ResultActions resultActions = mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        resultActions.andExpect(status().isOk());

        AppUser savedUser = userRepository.findByNetId(testUser).orElseThrow();

        assertThat(savedUser.getNetId()).isEqualTo(testUser);
        assertThat(savedUser.getPassword()).isEqualTo(testHashedPassword);
    }

    @Test
    public void applyFaculty_WithValidData() throws Exception {

        final NetId testUser = new NetId("SomeUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        registrationService.registerUser(testUser, testPassword);

        ApplyFacultyRequestModel model = new ApplyFacultyRequestModel();
        model.setNetId(testUser.toString());
        model.setFaculty(AppUser.Faculty.EWI.toString());
        //act
        //ResultActions res = mockMvc.perform("/")
        ResultActions resultActions = mockMvc.perform(post("/applyFaculty")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model)));
        //Assert
        resultActions.andExpect(status().isOk());
        AppUser savedUser = userRepository.findByNetId(testUser).orElseThrow();

        assertThat(savedUser.getFaculties().contains(AppUser.Faculty.EWI)).isTrue();
        assertThat(savedUser.getFaculties().size()).isEqualTo(1);
    }

    @Test
    public void applyFaculty_WithWrongUser() throws Exception {
        final NetId testUser = new NetId("SomeUser");

        ApplyFacultyRequestModel model = new ApplyFacultyRequestModel();
        model.setNetId(testUser.toString());
        model.setFaculty(AppUser.Faculty.EWI.toString());

        ResultActions resultActions = mockMvc.perform(post("/applyFaculty")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model)));
        resultActions.andExpect(status().isBadRequest());
        assertThat(userRepository.existsByNetId(testUser)).isFalse();
    }

    @Test
    public void applyFaculty_withWrongFaculty() throws Exception {
        final NetId testUser = new NetId("SomeUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        registrationService.registerUser(testUser, testPassword);

        ApplyFacultyRequestModel model = new ApplyFacultyRequestModel();
        model.setNetId(testUser.toString());
        model.setFaculty("EWW");

        ResultActions resultActions = mockMvc.perform(post("/applyFaculty")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model)));
        resultActions.andExpect(status().isBadRequest());
        assertThat(userRepository.existsByNetId(testUser)).isTrue();
        if (userRepository.findByNetId(testUser).isPresent()) {
            assertThat(userRepository.findByNetId(testUser).get().getFaculties().size()).isEqualTo(0);
        }
    }

    @Test
    public void removeFaculty_withWrongFaculty() throws Exception {
        final NetId testUser = new NetId("SomeUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        registrationService.registerUser(testUser, testPassword);
        registrationService.applyFacultyUser(testUser, AppUser.Faculty.EWI);
        ApplyFacultyRequestModel model = new ApplyFacultyRequestModel();
        model.setNetId(testUser.toString());
        model.setFaculty("EWW");

        ResultActions resultActions = mockMvc.perform(delete("/removeFaculty")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model)));
        resultActions.andExpect(status().isBadRequest());
        assertThat(userRepository.existsByNetId(testUser)).isTrue();
        if (userRepository.findByNetId(testUser).isPresent()) {
            assertThat(userRepository.findByNetId(testUser).get().getFaculties().size()).isEqualTo(1);
        }
    }

    @Test
    public void removeFaculty_WithWrongUser() throws Exception {
        final NetId testUser = new NetId("SomeUser");

        ApplyFacultyRequestModel model = new ApplyFacultyRequestModel();
        model.setNetId(testUser.toString());
        model.setFaculty(AppUser.Faculty.EWI.toString());


        ResultActions resultActions = mockMvc.perform(delete("/removeFaculty")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model)));
        resultActions.andExpect(status().isBadRequest());
        assertThat(userRepository.existsByNetId(testUser)).isFalse();
    }

    @Test
    public void removeFaculty_withCorrectData() throws Exception {
        final NetId testUser = new NetId("SomeUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        registrationService.registerUser(testUser, testPassword);
        registrationService.applyFacultyUser(testUser, AppUser.Faculty.EWI);
        registrationService.applyFacultyUser(testUser, AppUser.Faculty.IO);
        ApplyFacultyRequestModel model = new ApplyFacultyRequestModel();
        model.setNetId(testUser.toString());
        model.setFaculty("EWI");

        ResultActions resultActions = mockMvc.perform(delete("/removeFaculty")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model)));
        resultActions.andExpect(status().isOk());
        AppUser savedUser = userRepository.findByNetId(testUser).orElseThrow();
        assertThat(savedUser.getFaculties().size()).isEqualTo(1);
        assertThat(savedUser.getFaculties().contains(AppUser.Faculty.EWI)).isFalse();
    }

    @Test
    public void getFaculty_WithWrongUser() throws Exception {
        final NetId testUser = new NetId("SomeUser");

        GetFacultyRequestModel model = new GetFacultyRequestModel();
        model.setToken(testUser.toString());
        ResultActions resultActions = mockMvc.perform(post("/getUserFaculties")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model)));
        resultActions.andExpect(status().isBadRequest());
        assertThat(userRepository.existsByNetId(testUser)).isFalse();
    }


    @Test
    public void getFaculty_WithCorrectData() throws Exception {
        final NetId testUser = new NetId("SomeUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        registrationService.registerUser(testUser, testPassword);
        registrationService.applyFacultyUser(testUser, AppUser.Faculty.EWI);

        GetFacultyRequestModel model = new GetFacultyRequestModel();
        model.setToken("MockedToken");

        ResultActions resultActions = mockMvc.perform(post("/getUserFaculties")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(model)));

        MvcResult result = resultActions
                .andExpect(status().isOk())
                .andReturn();

        GetFacultyResponseModel responseModel = JsonUtil.deserialize(result.getResponse().getContentAsString(),
                GetFacultyResponseModel.class);
        assertThat(responseModel.getFaculties().contains("EWI")).isTrue();

    }

    @Test
    public void register_withExistingUser_throwsException() throws Exception {
        // Arrange
        final NetId testUser = new NetId("SomeUser");
        final Password newTestPassword = new Password("password456");
        final HashedPassword existingTestPassword = new HashedPassword("password123");

        AppUser existingAppUser = new AppUser(testUser, existingTestPassword);
        Role role = roleControlService.findByName("USER");
        existingAppUser.setRole(role);
        userRepository.save(existingAppUser);


        RegistrationRequestModel model = new RegistrationRequestModel();
        model.setNetId(testUser.toString());
        model.setPassword(newTestPassword.toString());

        // Act
        ResultActions resultActions = mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        resultActions.andExpect(status().isBadRequest());

        AppUser savedUser = userRepository.findByNetId(testUser).orElseThrow();

        assertThat(savedUser.getNetId()).isEqualTo(testUser);
        assertThat(savedUser.getPassword()).isEqualTo(existingTestPassword);
    }

    @Test
    public void login_withValidUser_returnsToken() throws Exception {
        // Arrange
        final NetId testUser = new NetId("SomeUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        when(mockAuthenticationManager.authenticate(argThat(authentication ->
                !testUser.toString().equals(authentication.getPrincipal())
                    || !testPassword.toString().equals(authentication.getCredentials())
        ))).thenThrow(new UsernameNotFoundException("User not found"));

        final String testToken = "testJWTToken";
        when(mockJwtTokenGenerator.generateToken(
            argThat(userDetails -> userDetails.getUsername().equals(testUser.toString())))
        ).thenReturn(testToken);

        AppUser appUser = new AppUser(testUser, testHashedPassword);
        Role role = roleControlService.findByName("USER");
        appUser.setRole(role);
        userRepository.save(appUser);

        AuthenticationRequestModel model = new AuthenticationRequestModel();
        model.setNetId(testUser.toString());
        model.setPassword(testPassword.toString());

        // Act
        ResultActions resultActions = mockMvc.perform(post("/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));


        // Assert
        MvcResult result = resultActions
                .andExpect(status().isOk())
                .andReturn();

        AuthenticationResponseModel responseModel = JsonUtil.deserialize(result.getResponse().getContentAsString(),
                AuthenticationResponseModel.class);

        assertThat(responseModel.getToken()).isEqualTo(testToken);

        verify(mockAuthenticationManager).authenticate(argThat(authentication ->
                testUser.toString().equals(authentication.getPrincipal())
                    && testPassword.toString().equals(authentication.getCredentials())));
    }

    @Test
    public void login_withNonexistentUsername_returns403() throws Exception {
        // Arrange
        final String testUser = "SomeUser";
        final String testPassword = "password123";

        when(mockAuthenticationManager.authenticate(argThat(authentication ->
                testUser.equals(authentication.getPrincipal())
                    && testPassword.equals(authentication.getCredentials())
        ))).thenThrow(new UsernameNotFoundException("User not found"));

        AuthenticationRequestModel model = new AuthenticationRequestModel();
        model.setNetId(testUser);
        model.setPassword(testPassword);

        // Act
        ResultActions resultActions = mockMvc.perform(post("/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        resultActions.andExpect(status().isUnauthorized());

        verify(mockAuthenticationManager).authenticate(argThat(authentication ->
                testUser.equals(authentication.getPrincipal())
                    && testPassword.equals(authentication.getCredentials())));

        verify(mockJwtTokenGenerator, times(0)).generateToken(any());
    }

    @Test
    public void login_withInvalidPassword_returns403() throws Exception {
        // Arrange
        final String testUser = "SomeUser";
        final String wrongPassword = "password1234";
        final String testPassword = "password123";
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        

        when(mockPasswordEncoder.hash(new Password(testPassword))).thenReturn(testHashedPassword);

        when(mockAuthenticationManager.authenticate(argThat(authentication ->
                testUser.equals(authentication.getPrincipal())
                    && wrongPassword.equals(authentication.getCredentials())
        ))).thenThrow(new BadCredentialsException("Invalid password"));

        AppUser appUser = new AppUser(new NetId(testUser), testHashedPassword);
        userRepository.save(appUser);

        AuthenticationRequestModel model = new AuthenticationRequestModel();
        model.setNetId(testUser);
        model.setPassword(wrongPassword);

        // Act
        ResultActions resultActions = mockMvc.perform(post("/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(model)));

        // Assert
        resultActions.andExpect(status().isUnauthorized());

        verify(mockAuthenticationManager).authenticate(argThat(authentication ->
                testUser.equals(authentication.getPrincipal())
                    && wrongPassword.equals(authentication.getCredentials())));

        verify(mockJwtTokenGenerator, times(0)).generateToken(any());
    }
}
