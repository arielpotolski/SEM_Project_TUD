package nl.tudelft.sem.template.authentication.communicationdata;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import nl.tudelft.sem.template.authentication.models.NotificationRequestModel;

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

    @Column()
    private State state;
    @Column()
    private Type type;
    @Column()
    private  Date date;
    @Column()
    private  String message;
    @Column()
    private  LocalDate timeReceived;

    /**
     * Constructor of a Notification.
     *
     * @param state State of request/job stored in the notification
     * @param date Date of request/job scheduled/cancelled
     * @param message alert or a message of the notification
     * @param type type of notification is what microservice it came from
     */
    public Notification(State state, Date date, String message, Type type, String netId) {
        this.netId = netId;
        this.state = state;
        this.date = date;
        this.message = message;
        this.type = type;
        this.timeReceived = LocalDate.now();
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
            return new Notification(s, d, data.getMessage(), t, data.getNetId());
        } catch (Exception a) {
            System.out.println(a.getMessage());
            throw new IllegalArgumentException();
        }

    }

    //    public State getState() {
    //        return state;
    //    }
    //
    //    public Type getType() {
    //        return type;
    //    }
    //
    //    public Date getDate() {
    //        return date;
    //    }
    //
    //    public String getMessage() {
    //        return message;
    //    }
    //
    //    public LocalDate getTimeReceived() {
    //        return timeReceived;
    //    }

    @Override
    public String toString() {
        return "Notification{"
                + "netId='" + netId + '\''
                + ", state=" + state
                + ", type=" + type
                + ", date=" + date
                + ", message='" + message + '\''
                + ", timeReceived=" + timeReceived
                + '}';
    }
}

