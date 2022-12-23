package nl.tudelft.sem.template.cluster.domain.events;

import java.util.List;
import lombok.Getter;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import org.springframework.context.ApplicationEvent;

/**
 * A DDD domain event that indicates that nodes were removed (if a singular node, it contains a list of one object.)
 */
@Getter
public class NodesWereRemovedEvent extends ApplicationEvent {

    private final List<Node> nodesRemovedFromCluster;

    public NodesWereRemovedEvent(Object source, List<Node> nodesRemovedFromCluster) {
        super(source);
        this.nodesRemovedFromCluster = nodesRemovedFromCluster;
    }
}
