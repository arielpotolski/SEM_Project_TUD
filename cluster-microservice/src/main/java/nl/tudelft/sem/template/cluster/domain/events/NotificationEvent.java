package nl.tudelft.sem.template.cluster.domain.events;

import lombok.Getter;
import lombok.Setter;
import nl.tudelft.sem.template.cluster.models.NotificationRequestModel;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class NotificationEvent extends ApplicationEvent {

    public static final long serialVersionUID = -213769420;
    private final String date;
    private final String type;
    private final String state;
    private final String message;
    private final String netId;

    /**
     * Initializes a new Notification event.
     *
     * @param source the source of the notification
     * @param date the date of the notification
     * @param type the type of the notification
     * @param state the state of the notification
     * @param message the message of the notification
     * @param netId the netId of the notification
     */
    public NotificationEvent(Object source, String date, String type,
                             String state, String message, String netId) {
        super(source);
        this.date = date;
        this.type = type;
        this.state = state;
        this.message = message;
        this.netId = netId;
    }

    /**
     * Transforms a notification event into a valid NotificationRequestModel.
     *
     * @return the NotificationRequestModel
     */
    public NotificationRequestModel toNotification() {
        return new NotificationRequestModel(this.date,
            this.type, this.state, this.message, this.netId);
    }
}
