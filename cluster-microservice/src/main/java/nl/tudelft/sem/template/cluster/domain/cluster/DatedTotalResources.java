package nl.tudelft.sem.template.cluster.domain.cluster;

import java.time.LocalDate;

/**
 * Spring Projection Interface to handle result of querying for total resources per day for given faculty.
 */
public interface DatedTotalResources {

    LocalDate getScheduled_Date();

    double getCpu_Resources();

    double getGpu_Resources();

    double getMemory_Resources();

}
