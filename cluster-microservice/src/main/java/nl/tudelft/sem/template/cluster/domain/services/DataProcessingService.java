package nl.tudelft.sem.template.cluster.domain.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import nl.tudelft.sem.template.cluster.domain.HasEvents;
import nl.tudelft.sem.template.cluster.domain.cluster.AvailableResourcesForDate;
import nl.tudelft.sem.template.cluster.domain.cluster.FacultyDatedTotalResources;
import nl.tudelft.sem.template.cluster.domain.cluster.FacultyTotalResources;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;
import nl.tudelft.sem.template.cluster.domain.cluster.JobScheduleRepository;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.cluster.NodeRepository;
import nl.tudelft.sem.template.cluster.domain.events.NodesWereRemovedEvent;
import nl.tudelft.sem.template.cluster.domain.providers.DateProvider;
import nl.tudelft.sem.template.cluster.models.FacultyDatedResourcesResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Provides access to information from the repositories and processes it for further use.
 */
@Service
public class DataProcessingService {

    private final transient NodeRepository nodeRepository;
    private final transient JobScheduleRepository jobScheduleRepository;

    /**
     * The provider for dates.
     */
    private final transient DateProvider dateProvider;

    /**
     * Creates this service object.
     *
     * @param nodeRepository the node repository to get node information from.
     * @param jobScheduleRepository the schedule repository to get schedule and available resource information from.
     * @param dateProvider the date provider for current date and tomorrow.
     */
    public DataProcessingService(NodeRepository nodeRepository, JobScheduleRepository jobScheduleRepository,
                                 DateProvider dateProvider) {
        this.nodeRepository = nodeRepository;
        this.jobScheduleRepository = jobScheduleRepository;
        this.dateProvider = dateProvider;
    }

    // NODE SECTION

    public int getNumberOfNodesInRepository() {
        return (int) this.nodeRepository.count();
    }

    public boolean existsByUrl(String url) {
        return this.nodeRepository.existsByUrl(url);
    }

    public boolean existsByFacultyId(String facultyId) {
        return this.nodeRepository.existsByFacultyId(facultyId);
    }

    public Node getByUrl(String url) {
        return this.nodeRepository.findByUrl(url);
    }

    public List<Node> getByFacultyId(String facultyId) {
        return this.getAllNodes().stream().filter(x -> x.getFacultyId().equals(facultyId)).collect(Collectors.toList());
    }

    public List<Node> getAllNodes() {
        return this.nodeRepository.findAll();
    }

    public void save(Node node) {
        this.nodeRepository.save(node);
    }

    public void deleteNode(Node node) {
        this.nodeRepository.delete(node);
    }

    public void deleteAllNodes() {
        this.nodeRepository.deleteAll();
    }

    /**
     * Returns all facultyIds that have any assigned nodes in the repository.
     *
     * @return a list of all faculties known to the cluster.
     */
    public List<String> getAllFaculties() {
        return this.nodeRepository.findTotalResourcesPerFaculty().stream()
                .map(FacultyTotalResources::getFaculty_Id)
                .collect(Collectors.toList());
    }

    public List<FacultyTotalResources> getAssignedResourcesPerFaculty() {
        return this.nodeRepository.findTotalResourcesPerFaculty();
    }

    /**
     * Gets and returns the total resources in the three categories assigned to the specified faculty.
     *
     * @param facultyId the facultyId for which to find assigned resources.
     *
     * @return the total assigned resources for this facultyId, as well as the facultyId.
     */
    public FacultyTotalResources getAssignedResourcesForGivenFaculty(String facultyId)
        throws IllegalArgumentException {

        if (getAllFaculties().contains(facultyId)) {
            return getAssignedResourcesPerFaculty().stream()
                .filter(x -> x.getFaculty_Id().equals(facultyId))
                .collect(Collectors.toList()).get(0);
        } else {
            throw new IllegalArgumentException("Faculty does not exist.");
        }
    }

    // SCHEDULE/JOB SECTION

    /**
     * Checks whether a job requested through the faculty with the given id exists in the schedule.
     *
     * @param facultyId the facultyId to look for in the repository.
     *
     * @return boolean indicating whether the faculty with the given id has already had jobs requested and scheduled
     * through it.
     */
    public boolean existsInScheduleByFacultyId(String facultyId) {
        return this.jobScheduleRepository.existsByFacultyId(facultyId);
    }

    /**
     * Checks whether there is any job currently in the schedule on the given day.
     *
     * @param date the date on which to look for jobs.
     *
     * @return boolean indicating whether there is any job scheduled for the given day.
     */
    public boolean existsInScheduleByScheduledFor(LocalDate date) {
        return this.jobScheduleRepository.existsByScheduledFor(date);
    }

    /**
     * Gets and returns a list of all scheduled jobs, present and past, that exist in the database.
     *
     * @return a list of all jobs from the repository.
     */
    public List<Job> getAllJobsFromSchedule() {
        return this.jobScheduleRepository.findAll();
    }

    public void saveInSchedule(Job job) {
        this.jobScheduleRepository.save(job);
    }

    public void deleteAllJobsScheduled() {
        this.jobScheduleRepository.deleteAll();
    }

    /**
     * Gets and returns the already reserved resources per faculty per day.
     *
     * @return list of object containing dates, facultyIds, and the three doubles, representing the reserved
     * resources for that faculty on that day.
     */
    public List<FacultyDatedTotalResources> getReservedResourcesPerFacultyPerDay() {
        return this.jobScheduleRepository.findResourcesRequiredForEachDay();
    }

    /**
     * Gets and returns the already reserved resources per faculty for a given day.
     *
     * @param date the date on which to check already reserved resources.
     *
     * @return list of object containing dates, facultyIds, and the three doubles, representing the reserved
     * resources for that faculty on the given day.
     */
    public List<FacultyDatedTotalResources> getReservedResourcesPerFacultyForGivenDay(LocalDate date) {
        return this.getReservedResourcesPerFacultyPerDay().stream()
                .filter(x -> x.getScheduled_Date().equals(date)).collect(Collectors.toList());
    }

    /**
     * Gets and returns the already reserved resources per day for the given faculty.
     *
     * @param facultyId the facultyId by which to look for reserved resources per day.
     *
     * @return list of object containing dates, facultyIds, and the three doubles, representing the reserved
     * resources for the given faculty per day.
     */
    public List<FacultyDatedTotalResources> getReservedResourcesPerDayForGivenFaculty(String facultyId) {
        return this.getReservedResourcesPerFacultyPerDay().stream()
                .filter(x -> x.getFaculty_Id().equals(facultyId)).collect(Collectors.toList());
    }

    /**
     * Gets and returns the total reserved resources for a given faculty for a given day. This should be a list of one
     * object.
     *
     * @param date the day on which to look for reserved resources.
     * @param facultyId the facultyId for which to look for reserved resources.
     *
     * @return a list of one object containing the day, facultyId, and reserved resources for that day and faculty.
     */
    public List<FacultyDatedTotalResources> getReservedResourcesForGivenDayForGivenFaculty(LocalDate date,
                                                                                           String facultyId) {
        return this.getReservedResourcesPerFacultyPerDay().stream()
                .filter(x -> x.getFaculty_Id().equals(facultyId))
                .filter(x -> x.getScheduled_Date().equals(date))
                .collect(Collectors.toList());
    }

    /**
     * Queries the schedule for the latest date that occurs.
     *
     * @return the latest date in the schedule.
     */
    public LocalDate findLatestDateWithReservedResources() {
        var latestDateInSchedule = this.jobScheduleRepository.findMaximumDate();

        // if null, there are no jobs scheduled - thus we can go only until tomorrow
        return latestDateInSchedule != null ? latestDateInSchedule : dateProvider.getTomorrow();
    }

    // SCHEDULE/JOB - AVAILABLE RESOURCES

    /**
     * Calculates and returns a list of available resources for each day for the given faculty.
     *
     * @param facultyId the faculty to perform the calculation for.
     *
     * @return a list of available resources for each date until the specified one.
     */
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public List<AvailableResourcesForDate> getAvailableResourcesForGivenFacultyUntilDay(String facultyId,
                                                                                        LocalDate until) {
        var assignedResources = this.nodeRepository.findTotalResourcesForGivenFaculty(facultyId);
        var availableResources = new ArrayList<AvailableResourcesForDate>();

        // for each day from tomorrow until specified date
        for (LocalDate day : this.dateProvider.getTomorrow().datesUntil(until.plusDays(1))
                .collect(Collectors.toList())) {
            AvailableResourcesForDate availableResourcesForTheDay;

            // if there are no jobs scheduled on the current date or for the current faculty, available resources are
            // full
            var reserved = this
                    .getReservedResourcesForGivenDayForGivenFaculty(day, facultyId);
            if (!this.jobScheduleRepository.existsByFacultyId(facultyId)
                    || !this.jobScheduleRepository.existsByScheduledFor(day)
                    || (reserved.isEmpty())) {
                availableResourcesForTheDay = new AvailableResourcesForDate(day,
                        assignedResources.getCpu_Resources(),
                        assignedResources.getGpu_Resources(),
                        assignedResources.getMemory_Resources()
                );
            } else {
                // there are jobs scheduled for the given date
                var reservedResources = reserved.get(0);
                availableResourcesForTheDay = new AvailableResourcesForDate(day,
                        assignedResources.getCpu_Resources() - reservedResources.getCpu_Resources(),
                        assignedResources.getGpu_Resources() - reservedResources.getGpu_Resources(),
                        assignedResources.getMemory_Resources() - reservedResources.getMemory_Resources()
                );
            }

            availableResources.add(availableResourcesForTheDay);
        }
        return availableResources;
    }

    /**
     * Accessed from an endpoint. Returns the available resources for each faculty for each day until last day in
     * schedule, inclusive.
     *
     * @return the list of FacultyDatedResourcesResponseModels containing available resources for each faculty for each
     * date from tomorrow until last in schedule, inclusive.
     */
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public List<FacultyDatedResourcesResponseModel> getAvailableResourcesForAllFacultiesForAllDays() {
        var faculties = this.getAllFaculties();
        var latestDate = this.findLatestDateWithReservedResources();
        List<FacultyDatedResourcesResponseModel> models = new ArrayList<>();
        for (String faculty : faculties) {
            var modelsForFaculty = this
                    .getAvailableResourcesForGivenFacultyUntilDay(faculty, latestDate);
            models.addAll(FacultyDatedResourcesResponseModel
                    .convertToResponseModelsWithSeparateFaculty(modelsForFaculty, faculty));
        }
        return models;
    }

    /**
     * Gets and returns the available resources per day for the given faculty.
     *
     * @param facultyId the facultyId by which to look for available resources per day.
     *
     * @return list of object containing dates, facultyIds, and the three doubles, representing the available
     * resources for the given faculty per day.
     */
    public List<FacultyDatedResourcesResponseModel> getAvailableResourcesForGivenFacultyForAllDays(String facultyId) {
        return this.getAvailableResourcesForAllFacultiesForAllDays().stream()
                .filter(x -> x.getFacultyId().equals(facultyId)).collect(Collectors.toList());
    }

    /**
     * Gets and returns the available resources per faculty for a given day.
     *
     * @param date the date on which to check available resources.
     *
     * @return list of object containing dates, facultyIds, and the three doubles, representing the available
     * resources for that faculty on the given day.
     */
    public List<FacultyDatedResourcesResponseModel> getAvailableResourcesForAllFacultiesForGivenDay(LocalDate date) {
        var res = this.getAvailableResourcesForAllFacultiesForAllDays().stream()
                .filter(x -> x.getDate().isEqual(date)).collect(Collectors.toList());
        if (res.isEmpty()) {
            return this.getAssignedResourcesPerFaculty().stream()
                    .map(x -> new FacultyDatedResourcesResponseModel(
                            date, x.getFaculty_Id(), x.getCpu_Resources(), x.getGpu_Resources(), x.getMemory_Resources()
                    )).collect(Collectors.toList());
        } else {
            return res;
        }
    }

    /**
     * Gets and returns the total available resources for a given faculty for a given day. This should be a list of one
     * object.
     *
     * @param date the day on which to look for available resources.
     * @param facultyId the facultyId for which to look for available resources.
     *
     * @return a list of one object containing the day, facultyId, and available resources for that day and faculty.
     */
    public List<FacultyDatedResourcesResponseModel> getAvailableResourcesForGivenFacultyForGivenDay(LocalDate date,
                                                                                                    String facultyId) {
        var res = this.getAvailableResourcesForAllFacultiesForAllDays().stream()
                .filter(x -> x.getDate().isEqual(date))
                .filter(x -> x.getFacultyId().equals(facultyId))
                .collect(Collectors.toList());
        if (res.isEmpty()) {
            return Stream.of(this.getAssignedResourcesForGivenFaculty(facultyId))
                    .map(x -> new FacultyDatedResourcesResponseModel(
                            date, x.getFaculty_Id(), x.getCpu_Resources(), x.getGpu_Resources(), x.getMemory_Resources()
                    )).collect(Collectors.toList());
        } else {
            return res;
        }
    }

}
