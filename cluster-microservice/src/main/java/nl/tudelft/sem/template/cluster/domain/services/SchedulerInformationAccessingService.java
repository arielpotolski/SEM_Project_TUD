package nl.tudelft.sem.template.cluster.domain.services;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.cluster.domain.cluster.AvailableResourcesForDate;
import nl.tudelft.sem.template.cluster.domain.cluster.FacultyDatedTotalResources;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;
import nl.tudelft.sem.template.cluster.domain.cluster.JobScheduleRepository;
import nl.tudelft.sem.template.cluster.domain.cluster.NodeRepository;
import nl.tudelft.sem.template.cluster.domain.providers.DateProvider;
import nl.tudelft.sem.template.cluster.models.TotalResourcesResponseModel;
import org.springframework.stereotype.Service;


@Service
public class SchedulerInformationAccessingService {

    /**
     * The schedule repository to save scheduled jobs into.
     */
    private final transient JobScheduleRepository jobScheduleRepo;

    /**
     * The node repository containing all the cluster's nodes.
     */
    private final transient NodeRepository nodeRepo;

    /**
     * The provider for dates.
     */
    private final transient DateProvider date;

    /**
     * Constructs a new instance of this service.
     *
     * @param jobScheduleRepo the schedule repository.
     * @param nodeRepo the node repository.
     * @param date the date provider.
     */
    public SchedulerInformationAccessingService(JobScheduleRepository jobScheduleRepo, NodeRepository nodeRepo,
                                                DateProvider date) {
        this.jobScheduleRepo = jobScheduleRepo;
        this.nodeRepo = nodeRepo;
        this.date = date;
    }

    /**
     * Checks whether a job requested through the faculty with the given id exists in the schedule.
     *
     * @param facultyId the facultyId to look for in the repository.
     *
     * @return boolean indicating whether the faculty with the given id has already had jobs requested and scheduled
     * through it.
     */
    public boolean existsByFacultyId(String facultyId) {
        return this.jobScheduleRepo.existsByFacultyId(facultyId);
    }

    /**
     * Checks whether there is any job currently in the schedule on the given day.
     *
     * @param date the date on which to look for jobs.
     *
     * @return boolean indicating whether there is any job scheduled for the given day.
     */
    public boolean existsByScheduledFor(LocalDate date) {
        return this.jobScheduleRepo.existsByScheduledFor(date);
    }

    /**
     * Gets and returns a list of all scheduled jobs, present and past, that exist in the database.
     *
     * @return a list of all jobs from the repository.
     */
    public List<Job> getAllJobsFromSchedule() {
        return this.jobScheduleRepo.findAll();
    }

    /**
     * Converts the input from Spring Projection Interfaces from SQL queries into response models to be returned through
     * HTTP.
     *
     * @param rawData the data to convert.
     *
     * @return converted data as a list.
     */
    public List<TotalResourcesResponseModel> convertToResponseModels(List<FacultyDatedTotalResources> rawData) {
        return rawData.stream().map(x -> new TotalResourcesResponseModel(x.getScheduled_Date(),
                        x.getFaculty_Id(), x.getCpu_Resources(),
                        x.getGpu_Resources(), x.getMemory_Resources()))
                .collect(Collectors.toList());
    }

    /**
     * Gets and returns the already reserved resources per faculty per day.
     *
     * @return list of object containing dates, facultyIds, and the three doubles, representing the reserved
     * resources for that faculty on that day.
     */
    public List<FacultyDatedTotalResources> getReservedResourcesPerFacultyPerDay() {
        return this.jobScheduleRepo.findResourcesRequiredForEachDay();
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
     * Calculates and returns a list of available resources for each day for the given faculty. The PMD warning
     * is suppressed because that feature has been deprecated and yet it still runs that check for some reason.
     *
     * @param facultyId the faculty to perform the calculation for.
     *
     * @return a list of available resources for each date until the specified one.
     */
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public List<AvailableResourcesForDate> getAvailableResourcesForGivenFacultyUntilDay(String facultyId,
                                                                                        LocalDate until) {
        var assignedResources = nodeRepo.findTotalResourcesForGivenFaculty(facultyId);
        var availableResources = new ArrayList<AvailableResourcesForDate>();

        // for each day from tomorrow until specified date
        for (LocalDate day : date.getTomorrow().datesUntil(until.plusDays(1)).collect(Collectors.toList())) {
            AvailableResourcesForDate availableResourcesForTheDay;
            if (!jobScheduleRepo.existsByFacultyId(facultyId) || !jobScheduleRepo.existsByScheduledFor(day)) {
                availableResourcesForTheDay = new AvailableResourcesForDate(day,
                        assignedResources.getCpu_Resources(),
                        assignedResources.getGpu_Resources(),
                        assignedResources.getMemory_Resources()
                );
            } else {
                var reservedResources = this.getReservedResourcesForGivenDayForGivenFaculty(day, facultyId).get(0);
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
}
