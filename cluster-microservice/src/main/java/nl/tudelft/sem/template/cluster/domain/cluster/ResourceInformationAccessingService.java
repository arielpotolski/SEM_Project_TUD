package nl.tudelft.sem.template.cluster.domain.cluster;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.cluster.domain.providers.DateProvider;
import org.springframework.stereotype.Service;


@Service
public class ResourceInformationAccessingService {

    /**
     * The schedule repository to save scheduled jobs into.
     */
    private final transient JobScheduleRepository jobScheduleRepo;

    /**
     * The node repository containing all the cluster's nodes.
     */
    private final transient NodeRepository nodeRepo;

    private final transient DateProvider date;

    /**
     * Constructs a new instance of this service.
     *
     * @param jobScheduleRepo the schedule repository.
     * @param nodeRepo the node repository.
     * @param date the date provider.
     */
    public ResourceInformationAccessingService(JobScheduleRepository jobScheduleRepo, NodeRepository nodeRepo,
                                               DateProvider date) {
        this.jobScheduleRepo = jobScheduleRepo;
        this.nodeRepo = nodeRepo;
        this.date = date;
    }

    /**
     * TODO: use functional Java to generalize this
     * Calculates and returns a list of available resources for each day for the given faculty.
     *
     * @param facultyId the faculty to perform the calculation for.
     *
     * @return a list of available resources for each date until the specified one.
     */
    public List<AvailableResourcesForDate> getAvailableResourcesForGivenFacultyUntilDay(String facultyId,
                                                                                        LocalDate until) {
        var assignedResources = nodeRepo.findTotalResourcesForGivenFaculty(facultyId);
        var availableResources = new ArrayList<AvailableResourcesForDate>();

        // for each day from tomorrow until specified date
        for (LocalDate day : date.getTomorrow().datesUntil(until.plusDays(1)).collect(Collectors.toList())) {
            var reservedResources = jobScheduleRepo
                    .findResourcesRequiredForGivenFacultyForGivenDay(day, facultyId);
            // TODO: what does this return when there's none reserved? ^
            AvailableResourcesForDate availableResourcesForTheDay;
            if (reservedResources == null) {
                availableResourcesForTheDay = new AvailableResourcesForDate(day,
                        assignedResources.getCpu_Resources(),
                        assignedResources.getGpu_Resources(),
                        assignedResources.getMemory_Resources()
                );
            } else {
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
