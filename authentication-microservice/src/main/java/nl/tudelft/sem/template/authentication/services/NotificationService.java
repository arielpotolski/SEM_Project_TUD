package nl.tudelft.sem.template.authentication.services;

import java.util.List;
import nl.tudelft.sem.template.authentication.communicationdata.Notification;
import nl.tudelft.sem.template.authentication.domain.user.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class to store, handle, and sent out incoming notifications.
 */
@Service
public class NotificationService {


    private final transient NotificationRepository notificationRepository;

    /**
     * Constructor which initializes the data storage.
     */
    @Autowired
    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Method which stores an incoming notification.
     *
     * @param notificationData all the data fields of the notification
     */
    public void addNotification(Notification notificationData) {
        notificationRepository.save(notificationData);
    }

    /**
     * Method which will return a list containing all notifications of the user, or an empty list if there are none.
     *
     * @param netId netId of the user
     * @return list containing notifications, can be emtpy.
     */
    public List<Notification> getNotifications(String netId) {
        return notificationRepository.findByNetId(netId);
    }

}
