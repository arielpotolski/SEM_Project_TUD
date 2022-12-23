package nl.tudelft.sem.template.cluster.listeners;

import nl.tudelft.sem.template.cluster.domain.events.NotificationEvent;
import nl.tudelft.sem.template.cluster.domain.services.NotificationManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener implements ApplicationListener<NotificationEvent> {

    private transient NotificationManagerService notificationManagerService;


    @Autowired
    public NotificationEventListener(NotificationManagerService notificationManagerService) {
        this.notificationManagerService = notificationManagerService;
    }

    @Override
    public void onApplicationEvent(NotificationEvent notificationEvent) {
        this.notificationManagerService.sendNotification(notificationEvent.toNotification());
    }
}
