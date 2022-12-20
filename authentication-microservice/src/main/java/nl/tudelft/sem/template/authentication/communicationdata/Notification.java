package nl.tudelft.sem.template.authentication.communicationdata;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import nl.tudelft.sem.template.authentication.models.NotificationRequestModel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Class which stores notifications data, and allows it to be stored in a repository.
 */
@Entity
@Table
@Getter
@Setter
public class Notification {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, unique = true)
    private Long id;

    @Column
    private String netId;
    @Column
    private State stateOfStatus;
    @Column
    private Type notificationOrigin;
    @Column(columnDefinition = "DATE")
    private  Date dateCreated;
    @Column
    private  String message;
    @Column
    private  LocalDate timeReceived;



    /**
     * Constructor of a Notification.
     *
     * @param state State of request/job stored in the notification
     * @param date Date of request/job scheduled/cancelled
     * @param message alert or a message of the notification
     * @param type type of notification is what microservice it came from
     */
    public Notification(State state, Date date, String message, Type type, String netId, LocalDate timeCreated) {
        this.netId = netId;
        this.stateOfStatus = state;
        this.dateCreated = date;
        this.message = message;
        this.timeReceived = timeCreated;
        this.notificationOrigin = type;
    }

    public Notification() {
    }

    /**
     * Factory that creates a Notification from a notificationRequestModel.
     *
     * @param data all the data from the request
     * @return Notification
     * @throws IllegalArgumentException if the data is not valid
     */
    public static Notification createNotification(NotificationRequestModel data) throws IllegalArgumentException {
        State s;
        Date d;
        Type t;

        try {
            s = State.valueOf(data.getState());
            d = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(data.getDate());
            t = Type.valueOf(data.getType());
            return new Notification(s, d, data.getMessage(), t, data.getNetId(), LocalDate.now());
        } catch (Exception a) {
            System.out.println(a.getMessage());
            throw new IllegalArgumentException();
        }

    }

    @Override
    public String toString() {
        return "Notification{"
               + "id=" + id
                + ", netId='" + netId + '\''
                + ", state=" + stateOfStatus
                + ", type=" + notificationOrigin
                + ", date=" + dateCreated
                + ", message='" + message + '\''
                + ", timeReceived=" + timeReceived
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Notification)) {
            return false;
        }
        Notification that = (Notification) o;
        return Objects.equals(id, that.id) && Objects.equals(netId, that.netId)
                && stateOfStatus == that.stateOfStatus && notificationOrigin == that.notificationOrigin
                && Objects.equals(message, that.message) && Objects.equals(timeReceived, that.timeReceived)
                && (this.dateCreated.getTime() == that.dateCreated.getTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, netId, stateOfStatus, notificationOrigin, dateCreated, message, timeReceived);
    }
}

