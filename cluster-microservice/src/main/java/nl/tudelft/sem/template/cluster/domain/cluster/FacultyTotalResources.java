package nl.tudelft.sem.template.cluster.domain.cluster;

/**
 * Spring Projection Interface to handle result of querying for total resources per faculty.
 */
public interface FacultyTotalResources {

    String getFaculty_Id();
    double getCpu_Resources();
    double getGpu_Resources();
    double getMemory_Resources();

}
