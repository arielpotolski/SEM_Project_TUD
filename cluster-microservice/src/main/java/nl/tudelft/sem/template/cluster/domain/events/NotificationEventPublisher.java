package nl.tudelft.sem.template.cluster.domain.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventPublisher {

    @Autowired
    private transient ApplicationEventPublisher applicationEventPublisher;

    public void publishCustomEvent(final String date, final String type, final String state, final String message,
                                   final String netId) {
        NotificationEvent event = new NotificationEvent(this, date, type, state, message, netId);
        applicationEventPublisher.publishEvent(event);
    }

}
