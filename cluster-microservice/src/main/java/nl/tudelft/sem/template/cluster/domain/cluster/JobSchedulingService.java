package nl.tudelft.sem.template.cluster.domain.cluster;

import nl.tudelft.sem.template.cluster.domain.strategies.EarliestPossibleDateStrategy;
import nl.tudelft.sem.template.cluster.domain.strategies.JobSchedulingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service handles scheduling a job according to the current strategy.
 */
@Service
public class JobSchedulingService {

    private final transient JobScheduleRepository repo;
    private transient JobSchedulingStrategy strategy;

    @Autowired
    public JobSchedulingService(JobScheduleRepository repo) {
        this.repo = repo;

        // default strategy: first come, first served, the earliest possible date
        this.strategy = new EarliestPossibleDateStrategy();
    }

    public void changeSchedulingStrategy(JobSchedulingStrategy strategy) {
        this.strategy = strategy;
    }





}
