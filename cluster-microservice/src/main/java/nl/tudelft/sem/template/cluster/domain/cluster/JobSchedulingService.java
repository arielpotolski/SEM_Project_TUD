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

    /**
     * The schedule repository to save scheduled jobs into.
     */
    private final transient JobScheduleRepository repo;

    /**
     * Current strategy of scheduling jobs.
     */
    private transient JobSchedulingStrategy strategy;

    /**
     * Creates a new JobSchedulingService object and injects the repository.
     *
     * @param repo the JobScheduleRepository that jobs are scheduled in.
     */
    @Autowired
    public JobSchedulingService(JobScheduleRepository repo) {
        this.repo = repo;

        // default strategy: first come, first served; the earliest possible date
        this.strategy = new EarliestPossibleDateStrategy();
    }

    /**
     * Changes the scheduling strategy to the provided one.
     *
     * @param strategy the scheduling strategy by which this service will be scheduling jobs.
     */
    public void changeSchedulingStrategy(JobSchedulingStrategy strategy) {
        this.strategy = strategy;
    }





}
