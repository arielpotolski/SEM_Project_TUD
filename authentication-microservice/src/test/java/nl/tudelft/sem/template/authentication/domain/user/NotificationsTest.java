package nl.tudelft.sem.template.authentication.domain.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Locale;
import nl.tudelft.sem.template.authentication.communicationdata.Notification;
import nl.tudelft.sem.template.authentication.communicationdata.State;
import nl.tudelft.sem.template.authentication.communicationdata.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class NotificationsTest {

    @Test
    public void equalsTest() throws ParseException {
        Notification notification1 = new Notification(State.ACCEPTED, new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                .parse("2022-02-02"), "mes", Type.JOB, "TestUser", LocalDate.of(2022, 2, 1));
        Notification notification2 = new Notification(State.RESCHEDULED, new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                .parse("2022-02-02"), "mes", Type.JOB, "TestUser", LocalDate.of(2022, 2, 1));
        Notification notification3 = new Notification(State.ACCEPTED, new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                .parse("2022-02-01"), "mes", Type.JOB, "TestUser", LocalDate.of(2022, 2, 1));
        Notification notification4 = new Notification(State.ACCEPTED, new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                .parse("2022-02-02"), "mesi", Type.JOB, "TestUser", LocalDate.of(2022, 2, 1));
        Notification notification5 = new Notification(State.ACCEPTED, new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                .parse("2022-02-02"), "mes", Type.REQUEST, "TestUser", LocalDate.of(2022, 2, 1));
        Notification notification6 = new Notification(State.ACCEPTED, new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                .parse("2022-02-02"), "mes", Type.JOB, "TestUser1", LocalDate.of(2022, 2, 1));
        Notification notification7 = new Notification(State.ACCEPTED, new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                .parse("2022-02-02"),  "mes", Type.JOB, "TestUser", LocalDate.of(2022, 1, 1));
        Notification notification8 = new Notification(State.ACCEPTED, new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                .parse("2022-02-02"), "mes", Type.JOB, "TestUser", LocalDate.of(2022, 2, 1));
        NetId testUser = new NetId("TestUser");
        assertThat(notification1.equals(notification1)).isTrue();
        assertThat(notification1.equals(notification8)).isTrue();
        assertThat(notification1.equals(notification2)).isFalse();
        assertThat(notification1.equals(notification3)).isFalse();
        assertThat(notification1.equals(notification4)).isFalse();
        assertThat(notification1.equals(notification5)).isFalse();
        assertThat(notification1.equals(notification6)).isFalse();
        assertThat(notification1.equals(notification7)).isFalse();
        assertThat(notification1.equals(testUser)).isFalse();

    }
}
