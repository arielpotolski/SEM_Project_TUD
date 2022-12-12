package nl.tudelft.sem.template.cluster.domain.cluster;

import java.time.LocalDate;

/**
 * Spring Projection Interface to handle result of querying for total resources per faculty for given day.
 */
public interface FacultyDatedTotalResources {

    String getFaculty_Id();

    double getCpu_Resources();

    double getGpu_Resources();

    double getMemory_Resources();

    LocalDate getScheduled_Date();

}
