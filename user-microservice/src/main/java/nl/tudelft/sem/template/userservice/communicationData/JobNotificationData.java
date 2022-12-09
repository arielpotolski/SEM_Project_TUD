package nl.tudelft.sem.template.userservice.communicationData;

import org.springframework.security.core.parameters.P;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

/**
 * Class to store the date of a incoming job Notification
 */
public class JobNotificationData {

    public State state;
    public Date date;
    public String message;


    public JobNotificationData(State state, Date date, String message) {
        this.state = state;
        this.date = date;
        this.message = message;
    }

    public static JobNotificationData createJobNotification(String state, String date, String message) throws IllegalArgumentException{
        State s;
        Date d;

        try {
            s = State.valueOf(state);
            System.out.println(s);
            d = new SimpleDateFormat("dd/MM/yyyy").parse(date);
            System.out.println(d);
            return new JobNotificationData(s, d, message);
        } catch (IllegalArgumentException a){
            System.out.println(a);
            throw new IllegalArgumentException();
        }catch (Exception e){
            System.out.println(e);
            throw new IllegalArgumentException();
        }

    }

    @Override
    public String toString() {
        return "JobNotificationData{" +
                "state=" + state +
                ", date=" + date +
                ", message='" + message + '\'' +
                '}';
    }
}
//local date from java.tuime

enum State{
    SCHEDULED,
    STARTED,
    COMPLETED,
    DELAYED,
    DROPPED
}
