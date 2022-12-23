package nl.tudelft.sem.template.cluster.application.cluster;

import java.util.stream.Collectors;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.events.NodesWereRemovedEvent;
import nl.tudelft.sem.template.cluster.domain.services.JobSchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * This event listener is automatically called when node(s) are removed from the cluster either directly by a sysadmin
 * or indirectly by users who schedule the removals.
 */
@Component
public class NodesWereRemovedEventListener implements ApplicationListener<NodesWereRemovedEvent> {

    private final transient JobSchedulingService jobSchedulingService;

    @Autowired
    public NodesWereRemovedEventListener(JobSchedulingService jobSchedulingService) {
        this.jobSchedulingService = jobSchedulingService;
    }

    /**
     * The name of the function indicated which event is listened to.
     *
     * @param event The event to react to
     */
    @Override
    public void onApplicationEvent(NodesWereRemovedEvent event) {
        for (Node node : event.getNodesRemovedFromCluster()) {
            System.out.println(node.getUrl() + " was removed from the cluster.");
        }
        var faculties = event.getNodesRemovedFromCluster().stream()
                .map(x -> x.getFacultyId()).distinct().collect(Collectors.toList());
        this.jobSchedulingService.rescheduleJobsForFacultiesWithRemovedNodes(faculties);
    }
}
