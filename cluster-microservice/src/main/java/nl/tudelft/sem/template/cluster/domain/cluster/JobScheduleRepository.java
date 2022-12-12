package nl.tudelft.sem.template.cluster.domain.cluster;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * A DDD repository for querying and persisting information related to the schedule of the cluster.
 */
@Repository
public interface JobScheduleRepository extends JpaRepository<Job, Long> {

    /**
     * Looks for all jobs which have been requested by the user with the provided netID.
     *
     * @param userNetId the netID to find the jobs by.
     *
     * @return a list of all jobs with a matching netID in the repository.
     */
    List<Job> findByUserNetId(String userNetId);

    /**
     * Checks if the user with the provided netID has any job in the repository.
     *
     * @param userNetId the netID to check the existence of.
     *
     * @return a boolean indicating whether there exists any job which has been requested by the user with
     * the given netID.
     */
    boolean existsByUserNetId(String userNetId);

    /**
     * Looks for all jobs which have been requested through the faculty with the given facultyID.
     *
     * @param facultyId the facultyID to find the jobs by.
     *
     * @return a list of all jobs with a matching facultyID in the repository.
     */
    List<Job> findByFacultyId(String facultyId);

    /**
     * Checks if any jobs have been requested through the faculty with given facultyID.
     *
     * @param facultyId the facultyID to check the existence of.
     *
     * @return a boolean indicating whether there exists any job which has been requested through the faculty with
     * the given facultyID.
     */
    boolean existsByFacultyId(String facultyId);

    /**
     * Looks for all jobs which have been scheduled for the given date.
     *
     * @param scheduledFor the date to find the jobs by.
     *
     * @return a list of all jobs scheduled for the given date.
     */
    List<Job> findByScheduledFor(LocalDate scheduledFor);

    /**
     * Checks if there exists any job scheduled for the provided date.
     *
     * @param scheduledFor the date to check.
     *
     * @return a boolean indicating whether there exists any job which has been scheduled on the given date.
     */
    boolean existsByScheduledFor(LocalDate scheduledFor);

    @Query(nativeQuery = true, value = "SELECT max(s.scheduled_for) FROM Schedule s")
    LocalDate findMaximumDate();

    /**
     * For each day in the scheduler (including the past) and for each faculty, returns the total number of resources
     * that have already been reserved. The warnings have been suppressed because the String literals are in queries
     * and thus should not be using a variable.
     *
     * @return list of FacultyDatedTotalResources, which contain the facultyId, the date and the resources reserved on
     * that day for that faculty.
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    @Query(nativeQuery = true,
            value = "SELECT s.faculty_id,"
                    + " s.scheduled_for AS Scheduled_Date,"
                    + " SUM(s.requiredCpu) AS Cpu_Resources,"
                    + " SUM(s.requiredGpu) AS Gpu_Resources,"
                    + " SUM(s.required_memory) AS Memory_Resources"
                    + " FROM Schedule s GROUP BY s.faculty_id, s.scheduled_for")
    List<FacultyDatedTotalResources> findResourcesRequiredForEachDay();

    /**
     * For the given day and for each faculty, returns the total number of resources that have already been reserved.
     *
     * @return list of FacultyTotalResources, which contain the facultyId and the resources reserved on
     * the given day for that faculty.
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    @Query(nativeQuery = true,
            value = "SELECT s.faculty_id,"
                    + " SUM(s.requiredCpu) AS Cpu_Resources,"
                    + " SUM(s.requiredGpu) AS Gpu_Resources,"
                    + " SUM(s.required_memory) AS Memory_Resources"
                    + " FROM Schedule s WHERE :date = s.scheduled_for"
                    + " GROUP BY s.faculty_id")
    List<FacultyTotalResources> findResourcesRequiredForGivenDay(@Param("date") LocalDate date);

    /**
     * For the given day and for each faculty, returns the total number of resources that have already been reserved.
     *
     * @return list of FacultyTotalResources, which contain the facultyId and the resources reserved on
     * the given day for that faculty.
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    @Query(nativeQuery = true,
            value = "SELECT s.scheduled_for AS Scheduled_Date,"
                    + " SUM(s.requiredCpu) AS Cpu_Resources,"
                    + " SUM(s.requiredGpu) AS Gpu_Resources,"
                    + " SUM(s.required_memory) AS Memory_Resources"
                    + " FROM Schedule s WHERE :facultyId = s.faculty_id"
                    + " GROUP BY s.scheduled_for")
    List<DatedTotalResources> findResourcesRequiredForGivenFaculty(@Param("facultyId") String facultyId);


    /**
     * For the given day and for given faculty, returns the total number of resources that have already been reserved.
     *
     * @return FacultyTotalResources, which contains the facultyId and the resources reserved on the given day for
     * the given faculty.
     */
    @SuppressWarnings("PMD.AvoidDuplicateLiterals")
    @Query(nativeQuery = true,
            value = "SELECT s.faculty_id,"
                    + " s.scheduled_for AS Scheduled_Date,"
                    + " SUM(s.requiredCpu) AS Cpu_Resources,"
                    + " SUM(s.requiredGpu) AS Gpu_Resources,"
                    + " SUM(s.required_memory) AS Memory_Resources"
                    + " FROM Schedule s WHERE :date = s.scheduled_for"
                    + " AND :facultyId = s.faculty_id"
                    + " GROUP BY s.faculty_id, s.scheduled_for")
    FacultyDatedTotalResources findResourcesRequiredForGivenFacultyForGivenDay(@Param("date") LocalDate date,
                                                                          @Param("facultyId") String facultyId);

}
