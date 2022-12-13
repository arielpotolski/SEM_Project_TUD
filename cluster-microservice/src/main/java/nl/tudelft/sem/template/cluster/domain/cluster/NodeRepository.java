package nl.tudelft.sem.template.cluster.domain.cluster;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * A DDD repository for querying and persisting cluster node information.
 */
@Repository
public interface NodeRepository extends JpaRepository<Node, Long> {

    /**
     * Tries to find and return a node by node url.
     *
     * @param url the url to find the node by.
     *
     * @return an optional containing the node if found.
     */
    Node findByUrl(String url);

    /**
     * Checks if a node exists with the given url.
     *
     * @param url the url to check the existence of.
     *
     * @return a boolean indicating whether a node with the given url exists in the repository.
     */
    boolean existsByUrl(String url);

    /**
     * Checks if the given faculty has any node assigned.
     *
     * @param facultyId the facultyId to check node assignment to.
     *
     * @return a boolean indicating whether any node is assigned to the given faculty.
     */
    boolean existsByFacultyId(String facultyId);


    /**
     * Returns a list of all faculty ids in the repository, along with the sum of the resources in each of the
     * three categories that are assigned to them.
     *
     * @return a list of objects which hold the facultyId and its sum of cpu, gpu, and memory resources.
     */
    @Query(nativeQuery = true,
            value = "SELECT n.faculty_id,"
                    + " SUM(n.cpu_resources) AS Cpu_Resources,"
                    + " SUM(n.gpu_resources) AS Gpu_Resources,"
                    + " SUM(n.memory_resources) AS Memory_Resources"
                    + " FROM Nodes n GROUP BY n.faculty_id")
    List<FacultyTotalResources> findTotalResourcesPerFaculty();

    /**
     * Returns the total resources assigned to the faculty with the provided id.
     *
     * @param facultyId the facultyId by which to look for resources.
     *
     * @return object which holds the facultyId and the sum of its cpu, gpu, and memory resources.
     */
    @Query(nativeQuery = true,
            value = "SELECT n.faculty_id,"
                    + " SUM(n.cpu_resources) AS Cpu_Resources,"
                    + " SUM(n.gpu_resources) AS Gpu_Resources,"
                    + " SUM(n.memory_resources) AS Memory_Resources"
                    + " FROM Nodes n WHERE n.faculty_id = :facultyId GROUP BY n.faculty_id LIMIT 1")
    FacultyTotalResources findTotalResourcesForGivenFaculty(@Param("facultyId") String facultyId);

    // add dedicated method to resources per node in the cluster?

    //

}
