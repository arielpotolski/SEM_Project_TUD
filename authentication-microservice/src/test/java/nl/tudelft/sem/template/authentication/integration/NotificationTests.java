package nl.tudelft.sem.template.authentication.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import nl.tudelft.sem.template.authentication.authentication.JwtTokenVerifier;
import nl.tudelft.sem.template.authentication.communicationdata.Notification;
import nl.tudelft.sem.template.authentication.integration.utils.JsonUtil;
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
import org.springframework.test.web.servlet.ResultActions;



@SpringBootTest
@ExtendWith(SpringExtension.class)
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockPasswordEncoder", "mockTokenGenerator", "mockAuthenticationManager", "mockTokenVerifier"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
public class NotificationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private transient NotificationService notificationService;


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
        notification.setId(1L);

        notificationService.addNotification(notification);

        assertThat(notificationService.getNotifications("coolUser1")).isEmpty();
        assertThat(notificationService.getNotifications("goodUser")).contains(notification);
    }

    @Test
    public void testMultipleNotifications() {

        Notification notification = Notification.createNotification(notificationRequestModel);
        Notification notification2 = Notification.createNotification(notificationRequestModel);
        notificationService.addNotification(notification);
        notificationService.addNotification(notification2);

        List<Notification> notifications = List.of(notification, notification2);
        assertThat(notificationService.getNotifications("goodUser")).containsExactlyElementsOf(notifications);
    }
}