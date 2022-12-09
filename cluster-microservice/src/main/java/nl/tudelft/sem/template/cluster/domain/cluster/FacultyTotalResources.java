package nl.tudelft.sem.template.cluster.domain.cluster;

/**
 * Spring Projection Interface to handle result of querying for total resources per faculty.
 */
public interface FacultyTotalResources {

    String getFacultyId();
    double getCpuResources();
    double getGpuResources();
    double getMemoryResources();

}
