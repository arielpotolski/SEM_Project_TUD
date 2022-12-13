package nl.tudelft.sem.template.authentication.communicationData;

import nl.tudelft.sem.template.authentication.models.NotificationRequestModel;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

/**
 * Class to store the date of a incoming job Notification
 */
public class Notification {

    private final State state;
    private final Type type;
    private final Date date;
    private final String message;
    private final LocalDate timeReceived;


    public Notification(State state, Date date, String message, Type type) {
        this.state = state;
        this.date = date;
        this.message = message;
        this.type = type;
        this.timeReceived = LocalDate.now();
    }

    public static Notification createNotification(NotificationRequestModel data) throws IllegalArgumentException{
        State s;
        Date d;
        Type t;

        try {
            s = State.valueOf(data.getState());
            d = new SimpleDateFormat("yyyy-MM-dd").parse(data.getDate());
            t = Type.valueOf(data.getType());
            return new Notification(s, d, data.getMessage(), t);
        } catch (IllegalArgumentException a){
            System.out.println(a);
            throw new IllegalArgumentException();
        }catch (Exception e){
            System.out.println(e);
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
        return "Notification{" +
                "state=" + state +
                ", type=" + type +
                ", date=" + date +
                ", message='" + message + '\'' +
                ", timeReceived=" + timeReceived +
                '}';
    }
}

enum State{
    SCHEDULED,
    STARTED,
    COMPLETED,
    DELAYED,
    DROPPED,
    ACCEPTED,
    REJECTED
}
enum Type{
    JOB,
    REQUEST
}
