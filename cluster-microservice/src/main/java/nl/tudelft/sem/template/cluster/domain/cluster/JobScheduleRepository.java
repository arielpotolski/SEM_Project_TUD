package nl.tudelft.sem.template.cluster.domain.cluster;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * A DDD repository for querying and persisting information related to the schedule of the cluster.
 */
@Repository
public interface JobScheduleRepository extends JpaRepository<Job, Long> {

    /**
     * Tries to find and return a job by job ID.
     *
     * @param id the id to find the job by.
     *
     * @return an optional containing the job if found.
     */
    Optional<Job> findById(long id);

    /**
     * Checks if a job exists with the given ID.
     *
     * @param id the id to check the existence of.
     *
     * @return a boolean indicating whether a job with the given ID exists in the repository.
     */
    boolean existsById(long id);

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

}
