package nl.tudelft.sem.template.authentication.communicationdata;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import nl.tudelft.sem.template.authentication.models.NotificationRequestModel;

/**
 * Class to store the date of a incoming job Notification.
 */
public class Notification {

    private final State state;
    private final Type type;
    private final Date date;
    private final String message;
    private final LocalDate timeReceived;

    /**
     * Constructor of a Notification.
     *
     * @param state State of request/job stored in the notification
     * @param date Date of request/job scheduled/cancelled
     * @param message alert or a message of the notification
     * @param type type of notification is what microservice it came from
     */
    public Notification(State state, Date date, String message, Type type) {
        this.state = state;
        this.date = date;
        this.message = message;
        this.type = type;
        this.timeReceived = LocalDate.now();
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
            d = new SimpleDateFormat("yyyy-MM-dd").parse(data.getDate());
            t = Type.valueOf(data.getType());
            return new Notification(s, d, data.getMessage(), t);
        } catch (Exception a) {
            System.out.println(a.getMessage());
            throw new IllegalArgumentException();
        }

    }

    public State getState() {
        return state;
    }

    public Type getType() {
        return type;
    }

    public Date getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    public LocalDate getTimeReceived() {
        return timeReceived;
    }

    @Override
    public String toString() {
        return "Notification{" + "state=" + state
                + ", type=" + type
                + ", date=" + date
                + ", message='" + message + '\''
                + ", timeReceived=" + timeReceived
                + '}';
    }
}

