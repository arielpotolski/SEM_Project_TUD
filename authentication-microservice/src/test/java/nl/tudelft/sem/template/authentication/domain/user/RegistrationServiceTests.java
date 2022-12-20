package nl.tudelft.sem.template.authentication.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import nl.tudelft.sem.template.authentication.services.PasswordHashingService;
import nl.tudelft.sem.template.authentication.services.RegistrationService;
import nl.tudelft.sem.template.authentication.services.RoleControlService;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockPasswordEncoder"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RegistrationServiceTests {

    @Autowired
    private transient RegistrationService registrationService;

    @Autowired
    private transient PasswordHashingService mockPasswordEncoder;

    @Autowired
    private transient RoleControlService roleControlService;

    @Autowired
    private transient UserRepository userRepository;

    /**
     * Sets up the tests.
     */
    @BeforeEach
    public void setup() {
        this.roleControlService.save(new Role("USER"));
        this.roleControlService.save(new Role("FACULTY"));
        this.roleControlService.save(new Role("SYSADMIN"));
        this.roleControlService.save(new Role("SYSTEM"));
    }

    @Test
    public void createUser_withValidData_worksCorrectly() throws Exception {
        // Arrange
        final NetId testUser = new NetId("SomeUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);


        // Act
        registrationService.registerUser(testUser, testPassword);

        // Assert
        AppUser savedUser = userRepository.findByNetId(testUser).orElseThrow();

        assertThat(savedUser.getNetId()).isEqualTo(testUser);
        assertThat(savedUser.getPassword()).isEqualTo(testHashedPassword);
        assertThat(savedUser.getRole()).isEqualTo(new Role("USER"));
    }

    @Test
    public void createUser_withExistingUser_throwsException() {
        // Arrange
        final NetId testUser = new NetId("SomeUser");
        final HashedPassword existingTestPassword = new HashedPassword("password123");
        final Password newTestPassword = new Password("password456");

        AppUser existingAppUser = new AppUser(testUser, existingTestPassword);
        userRepository.save(existingAppUser);

        // Act
        ThrowingCallable action = () -> registrationService.registerUser(testUser, newTestPassword);

        // Assert
        assertThatExceptionOfType(Exception.class)
                .isThrownBy(action);

        AppUser savedUser = userRepository.findByNetId(testUser).orElseThrow();

        assertThat(savedUser.getNetId()).isEqualTo(testUser);
        assertThat(savedUser.getPassword()).isEqualTo(existingTestPassword);
    }

    @Test

    public void assignFaculty_withCorrectData() throws Exception {
        final NetId testUser = new NetId("SomeUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);
        registrationService.registerUser(testUser, testPassword);
        registrationService.applyFacultyUser(testUser, AppUser.Faculty.EWI);
        //check
        AppUser savedUser = userRepository.findByNetId(testUser).orElseThrow();
        //asserts
        assertThat(savedUser.getFaculties().contains(AppUser.Faculty.EWI)).isTrue();
        assertThat(savedUser.getFaculties().size()).isEqualTo(1);

    }

    @Test
    public void assignFaculty_throwsExceptionWithWrongUser() {
        final NetId testUser = new NetId("SomeUser");

        ThrowingCallable action = () -> registrationService.applyFacultyUser(testUser, AppUser.Faculty.EWI);

        assertThatExceptionOfType(NetIdNotFoundException.class)
                .isThrownBy(action);
    }

    @Test
    public void getFaculty_withMultiple() throws Exception {
        final NetId testUser = new NetId("SomeUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);
        registrationService.registerUser(testUser, testPassword);
        registrationService.applyFacultyUser(testUser, AppUser.Faculty.EWI);
        registrationService.applyFacultyUser(testUser, AppUser.Faculty.IO);

        AppUser savedUser = userRepository.findByNetId(testUser).orElseThrow();
        assertThat(savedUser.getFaculties().contains(AppUser.Faculty.EWI)
                && savedUser.getFaculties().contains(AppUser.Faculty.IO)).isTrue();
        assertThat(savedUser.getFaculties().size()).isEqualTo(2);
    }

    @Test
    public void removeFaculty_withCorrectData() throws Exception {
        final NetId testUser = new NetId("SomeUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);
        registrationService.registerUser(testUser, testPassword);
        registrationService.applyFacultyUser(testUser, AppUser.Faculty.EWI);
        AppUser savedUser = userRepository.findByNetId(testUser).orElseThrow();
        assertThat(savedUser.getFaculties().contains(AppUser.Faculty.EWI)).isTrue();
        assertThat(savedUser.getFaculties().size()).isEqualTo(1);
        registrationService.removeFacultyUser(testUser, AppUser.Faculty.EWI);
        AppUser savedUser2 = userRepository.findByNetId(testUser).orElseThrow();
        assertThat(savedUser2.getFaculties().contains(AppUser.Faculty.EWI)).isFalse();
        assertThat(savedUser2.getFaculties().size()).isEqualTo(0);

    }

    @Test
    public void removeFaculty_WithWrongUser() {
        final NetId testUser = new NetId("SomeUser");
        ThrowingCallable action = () -> registrationService.removeFacultyUser(testUser, AppUser.Faculty.EWI);

        assertThatExceptionOfType(NetIdNotFoundException.class)
                .isThrownBy(action);
    }


}
