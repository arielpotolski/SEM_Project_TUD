package nl.tudelft.sem.template.authentication.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.time.LocalDate;
import java.util.Date;
import nl.tudelft.sem.template.authentication.authentication.JwtTokenVerifier;
import nl.tudelft.sem.template.authentication.authtemp.AuthManager;
import nl.tudelft.sem.template.authentication.communicationdata.Notification;
import nl.tudelft.sem.template.authentication.communicationdata.State;
import nl.tudelft.sem.template.authentication.communicationdata.Type;
import nl.tudelft.sem.template.authentication.integration.utils.JsonUtil;
import nl.tudelft.sem.template.authentication.models.GetNotifactionsRequestModel;
import nl.tudelft.sem.template.authentication.models.NotificationRequestModel;
import nl.tudelft.sem.template.authentication.services.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@ExtendWith(SpringExtension.class)
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockPasswordEncoder", "mockTokenGenerator", "mockAuthManager", "mockTokenVerifier"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class NotificationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private transient NotificationService notificationService;

    @Autowired
    private transient AuthManager authManager;

    @Autowired
    private transient JwtTokenVerifier mockJwtTokenVerifier;
    private NotificationRequestModel notificationRequestModel;


    /**
     * method setup.
     */
    @BeforeEach
    public void setupNotificationModel() {
        this.notificationRequestModel = new NotificationRequestModel();
        this.notificationRequestModel.setDate("2003-04-29");
        this.notificationRequestModel.setMessage("message");
        this.notificationRequestModel.setType("JOB");
        this.notificationRequestModel.setState("ACCEPTED");
        this.notificationRequestModel.setNetId("goodUser");


    }

    @Test
    public void testValidNotification() throws Exception {
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        ResultActions resultActions = mockMvc.perform(post("/notification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(notificationRequestModel))
                .header("Authorization", "Bearer MockedToken"));

        resultActions.andExpect(status().isOk());
    }

    @Test
    public void testInvalidNotificationDate() throws Exception {

        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        NotificationRequestModel invalid = notificationRequestModel;
        invalid.setDate("2003/01/23");

        ResultActions resultActions = mockMvc.perform(post("/notification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(notificationRequestModel))
                .header("Authorization", "Bearer MockedToken"));

        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    public void testInvalidNotificationEnum() throws Exception {

        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        NotificationRequestModel invalid = notificationRequestModel;
        invalid.setType("NOTASERVICE");

        ResultActions resultActions = mockMvc.perform(post("/notification")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JsonUtil.serialize(notificationRequestModel))
                .header("Authorization", "Bearer MockedToken"));

        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    public void testNotificationService() {

        Notification notification = Notification.createNotification(notificationRequestModel);
        notificationService.addNotification(notification);

        assertThat(notificationService.getNotifications("coolUser2")).isEmpty();
        assertThat(notificationService.getNotifications("coolUser1")).isEmpty();
    }

    @Test
    public void testMultipleNotifications() {

        Notification notification = Notification.createNotification(notificationRequestModel);
        notificationService.addNotification(notification);
        notificationService.addNotification(notification);
        notificationService.addNotification(notification);

        assertThat(notificationService.getNotifications("coolUser1")).isEmpty();
    }

    @Test
    public void testGetNotificationWithDateWhenCorrect() throws Exception {
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(authManager.getNetId()).thenReturn("user1");
        Notification notification1 = new Notification(State.ACCEPTED, new Date(2023, 02, 1),
                "mes", Type.JOB, "user1", LocalDate.of(2022, 10, 2));
        notificationService.addNotification(notification1);
        GetNotifactionsRequestModel requestModel = new GetNotifactionsRequestModel();
        requestModel.setDateFrom("2021-01-01");
        requestModel.setDateUntil("2023-01-01");

        ResultActions resultActions = mockMvc.perform(get("/getNotification")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(requestModel)));

        MvcResult result = resultActions
                .andExpect(status().isOk())
                .andReturn();

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String expected = ow.writeValueAsString(notification1);
        assertThat(result.getResponse().getContentAsString().contains(expected)).isTrue();
    }

    @Test
    public void testGetNotificationOnStartDate() throws Exception {
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(authManager.getNetId()).thenReturn("user1");
        Notification notification1 = new Notification(State.ACCEPTED, new Date(2023, 02,  1),
                "mes", Type.JOB, "user1", LocalDate.of(2022, 10, 2));
        notificationService.addNotification(notification1);
        GetNotifactionsRequestModel requestModel = new GetNotifactionsRequestModel();
        requestModel.setDateFrom("2021-01-01");
        requestModel.setDateUntil("2022-10-02");

        ResultActions resultActions = mockMvc.perform(get("/getNotification")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(requestModel)));

        MvcResult result = resultActions
                .andExpect(status().isOk())
                .andReturn();

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String expected = ow.writeValueAsString(notification1);
        assertThat(result.getResponse().getContentAsString().contains(expected)).isTrue();
    }

    @Test
    public void testGetNotificationOnEndDate() throws Exception {
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(authManager.getNetId()).thenReturn("user1");
        Notification notification1 = new Notification(State.ACCEPTED, new Date(2023, 02, 1),
                "mes", Type.JOB, "user1", LocalDate.of(2022, 10, 2));
        notificationService.addNotification(notification1);
        GetNotifactionsRequestModel requestModel = new GetNotifactionsRequestModel();
        requestModel.setDateFrom("2022-10-02");
        requestModel.setDateUntil("2023-10-02");

        ResultActions resultActions = mockMvc.perform(get("/getNotification")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(requestModel)));

        MvcResult result = resultActions
                .andExpect(status().isOk())
                .andReturn();

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String expected = ow.writeValueAsString(notification1);
        assertThat(result.getResponse().getContentAsString().contains(expected)).isTrue();
    }

    @Test
    public void testGetNotificationOutsideOfScope() throws Exception {
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(authManager.getNetId()).thenReturn("user1");
        Notification notification1 = new Notification(State.ACCEPTED, new Date(2023, 02, 1),
                "mes", Type.JOB, "user1", LocalDate.of(2022, 10, 2));
        notificationService.addNotification(notification1);
        GetNotifactionsRequestModel requestModel = new GetNotifactionsRequestModel();
        requestModel.setDateFrom("2021-01-01");
        requestModel.setDateUntil("2022-09-02");

        ResultActions resultActions = mockMvc.perform(get("/getNotification")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(requestModel)));

        MvcResult result = resultActions
                .andExpect(status().isOk())
                .andReturn();

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String expected = ow.writeValueAsString(notification1);
        assertThat(result.getResponse().getContentAsString().contains(expected)).isFalse();

    }

    @Test
    public void testGetNotificationNoEndDate() throws Exception {
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(authManager.getNetId()).thenReturn("user1");
        Notification notification1 = new Notification(State.ACCEPTED, new Date(2023, 02,  1),
                "mes", Type.JOB, "user1", LocalDate.of(2022, 10, 2));
        notificationService.addNotification(notification1);
        GetNotifactionsRequestModel requestModel = new GetNotifactionsRequestModel();
        requestModel.setDateUntil("2022-11-02");

        ResultActions resultActions = mockMvc.perform(get("/getNotification")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(requestModel)));

        MvcResult result = resultActions
                .andExpect(status().isOk())
                .andReturn();

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String expected = ow.writeValueAsString(notification1);
        assertThat(result.getResponse().getContentAsString().contains(expected)).isTrue();

    }

    @Test
    public void testGetNotificationNoStartDate() throws Exception {
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(authManager.getNetId()).thenReturn("user1");
        Notification notification1 = new Notification(State.ACCEPTED, new Date(2023, 02, 1),
                "mes", Type.JOB, "user1", LocalDate.of(2022, 10, 2));
        notificationService.addNotification(notification1);
        GetNotifactionsRequestModel requestModel = new GetNotifactionsRequestModel();
        requestModel.setDateFrom("2022-09-02");
        ResultActions resultActions = mockMvc.perform(get("/getNotification")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(requestModel)));

        MvcResult result = resultActions
                .andExpect(status().isOk())
                .andReturn();

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String expected = ow.writeValueAsString(notification1);
        assertThat(result.getResponse().getContentAsString().contains(expected)).isTrue();

    }

    @Test
    public void testGetNotificationOneOutsideOneInside() throws Exception {
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(authManager.getNetId()).thenReturn("user1");
        Notification notification1 = new Notification(State.ACCEPTED, new Date(2023, 02,  1),
                "mes", Type.JOB, "user1", LocalDate.of(2022, 10, 2));
        Notification notification2 = new Notification(State.ACCEPTED, new Date(2023, 02,  1),
                "mes", Type.JOB, "user1", LocalDate.of(2023, 10, 2));
        notificationService.addNotification(notification1);
        notificationService.addNotification(notification2);
        GetNotifactionsRequestModel requestModel = new GetNotifactionsRequestModel();
        requestModel.setDateUntil("2022-11-02");
        requestModel.setDateFrom("2020-11-02");

        ResultActions resultActions = mockMvc.perform(get("/getNotification")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(requestModel)));

        MvcResult result = resultActions
                .andExpect(status().isOk())
                .andReturn();

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String expected = ow.writeValueAsString(notification1);
        assertThat(result.getResponse().getContentAsString().contains(expected)).isTrue();

    }

    @Test
    public void testGetNotificationTwoNotifications() throws Exception {
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(authManager.getNetId()).thenReturn("user1");
        Notification notification1 = new Notification(State.ACCEPTED, new Date(2023, 02,  1),
                "mes", Type.JOB, "user1", LocalDate.of(2022, 10, 2));
        Notification notification2 = new Notification(State.ACCEPTED, new Date(2023, 02,  1),
                "mes", Type.JOB, "user1", LocalDate.of(2021, 10, 2));
        notificationService.addNotification(notification1);
        notificationService.addNotification(notification2);
        GetNotifactionsRequestModel requestModel = new GetNotifactionsRequestModel();
        requestModel.setDateUntil("2022-11-02");
        requestModel.setDateFrom("2020-11-02");

        ResultActions resultActions = mockMvc.perform(get("/getNotification")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(requestModel)));

        MvcResult result = resultActions
                .andExpect(status().isOk())
                .andReturn();

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String expected = ow.writeValueAsString(notification1);
        String expected2 = ow.writeValueAsString(notification2);
        assertThat(result.getResponse().getContentAsString().contains(expected)).isTrue();
        assertThat(result.getResponse().getContentAsString().contains(expected2)).isTrue();

    }

    @Test
    public void testGetNotificationNoOtherUsers() throws Exception {
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(authManager.getNetId()).thenReturn("user1");
        Notification notification1 = new Notification(State.ACCEPTED, new Date(2023, 02, 1),
                "mes", Type.JOB, "user1", LocalDate.of(2022, 10, 2));
        Notification notification2 = new Notification(State.ACCEPTED, new Date(2023, 02, 1),
                "mes", Type.JOB, "user", LocalDate.of(2021, 10, 2));
        notificationService.addNotification(notification1);
        notificationService.addNotification(notification2);
        GetNotifactionsRequestModel requestModel = new GetNotifactionsRequestModel();
        requestModel.setDateUntil("2022-11-02");
        requestModel.setDateFrom("2020-11-02");

        ResultActions resultActions = mockMvc.perform(get("/getNotification")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer MockedToken")
                .content(JsonUtil.serialize(requestModel)));

        MvcResult result = resultActions
                .andExpect(status().isOk())
                .andReturn();

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String expected = ow.writeValueAsString(notification1);
        String expected2 = ow.writeValueAsString(notification2);
        assertThat(result.getResponse().getContentAsString().contains(expected)).isTrue();
        assertThat(result.getResponse().getContentAsString().contains(expected2)).isFalse();

    }

    @Test
    public void testGetNotificationNoJsonSent() throws Exception {
        when(mockJwtTokenVerifier.validateToken(anyString())).thenReturn(true);
        when(authManager.getNetId()).thenReturn("user1");
        Notification notification1 = new Notification(State.ACCEPTED, new Date(2023, 02,  1),
                "mes", Type.JOB, "user1", LocalDate.of(2022, 10, 2));
        Notification notification2 = new Notification(State.ACCEPTED, new Date(2023, 02,  1),
                "mes", Type.JOB, "user1", LocalDate.of(2021, 10, 2));
        notificationService.addNotification(notification1);
        notificationService.addNotification(notification2);

        ResultActions resultActions = mockMvc.perform(get("/getNotification")
                .header("Authorization", "Bearer MockedToken"));


        MvcResult result = resultActions
                .andExpect(status().isOk())
                .andReturn();

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String expected = ow.writeValueAsString(notification1);
        String expected2 = ow.writeValueAsString(notification2);
        assertThat(result.getResponse().getContentAsString().contains(expected)).isTrue();
        assertThat(result.getResponse().getContentAsString().contains(expected2)).isTrue();

    }



}