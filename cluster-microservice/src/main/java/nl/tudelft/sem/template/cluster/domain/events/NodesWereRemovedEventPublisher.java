package nl.tudelft.sem.template.cluster.domain.events;

import java.util.List;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class NodesWereRemovedEventPublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void publishCustomEvent(final List<Node> removed) {
        NodesWereRemovedEvent event = new NodesWereRemovedEvent(this, removed);
        applicationEventPublisher.publishEvent(event);
    }
}
