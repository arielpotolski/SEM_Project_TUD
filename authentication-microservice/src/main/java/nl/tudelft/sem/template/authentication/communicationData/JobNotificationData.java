package nl.tudelft.sem.template.authentication.communicationData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.time.LocalTime;

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

    public static JobNotificationData createJobNotification(Data data) throws IllegalArgumentException{
        State s;
        Date d;

        try {
            s = State.valueOf(data.state);
            d = new SimpleDateFormat("dd/MM/yyyy").parse(data.date);
            return new JobNotificationData(s, d, data.message);
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
