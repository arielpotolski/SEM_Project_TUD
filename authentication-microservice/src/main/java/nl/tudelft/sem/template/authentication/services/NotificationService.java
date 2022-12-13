package nl.tudelft.sem.template.authentication.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.tudelft.sem.template.authentication.communicationdata.Notification;
import org.springframework.stereotype.Service;

/**
 * Service class to store, handle, and sent out incoming notifications.
 */
@Service
public class NotificationService {

    private final Map<String, List<Notification>> jobNotifications;

    /**
     * Constructor which initializes the data storage.
     */
    public NotificationService() {
        this.jobNotifications = new HashMap<>();
    }

    /**
     * Method which stores an incoming notification.
     *
     * @param netId netId of the user of which the notification belongs to
     * @param notificationData all the data fields of the notification
     */
    public void addNotification(String netId, Notification notificationData) {
        if (!jobNotifications.containsKey(netId)) {
            jobNotifications.put(netId, List.of(notificationData));

        } else {
            List<Notification> l = new ArrayList<>(jobNotifications.get(netId));
            l.add(notificationData);
            jobNotifications.put(netId, l);
        }
    }

    /**
     * Method which will return a list containing all notifications of the user, or an empty list if there are none.
     *
     * @param netId netId of the user
     * @return list containing notifications, can be emtpy.
     */
    public List<Notification> getNotifications(String netId) {
        if (jobNotifications.containsKey(netId)) {
            List<Notification> l =  jobNotifications.get(netId);
            jobNotifications.remove(netId);
            return l;
        } else {
            return new ArrayList<>();
        }
    }

}
