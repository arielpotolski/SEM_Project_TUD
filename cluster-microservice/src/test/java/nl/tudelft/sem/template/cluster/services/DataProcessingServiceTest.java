package nl.tudelft.sem.template.cluster.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.template.cluster.domain.builders.JobBuilder;
import nl.tudelft.sem.template.cluster.domain.builders.NodeBuilder;
import nl.tudelft.sem.template.cluster.domain.cluster.AvailableResourcesForDate;
import nl.tudelft.sem.template.cluster.domain.cluster.FacultyDatedTotalResources;
import nl.tudelft.sem.template.cluster.domain.cluster.FacultyTotalResources;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.providers.DateProvider;
import nl.tudelft.sem.template.cluster.domain.services.DataProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DataProcessingServiceTest {

    @Autowired
    private transient DataProcessingService dataProcessingService;

    @Autowired
    private transient DateProvider dateProvider;

    private Node node1;
    private Node node2;
    private Node node3;

    private Job job1;
    private Job job2;
    private Job job3;
    private Job job4;

    /**
     * Set up the tests.
     */
    @BeforeEach
    void setup() {
        this.dataProcessingService.deleteAllNodes();
        this.dataProcessingService.deleteAllJobsScheduled();

        this.node1 = new NodeBuilder()
            .setNodeCpuResourceCapacityTo(0.0)
            .assignToFacultyWithId("EWI")
            .setNodeGpuResourceCapacityTo(0.0)
            .setNodeMemoryResourceCapacityTo(0.0)
            .withNodeName("FacultyCentralCore")
            .foundAtUrl("/" + "EWI" + "/central-core")
            .byUserWithNetId("SYSTEM")
            .constructNodeInstance();
        this.node2 = new NodeBuilder()
            .setNodeCpuResourceCapacityTo(0.0)
            .setNodeGpuResourceCapacityTo(0.0)
            .setNodeMemoryResourceCapacityTo(0.0)
            .withNodeName("FacultyCentralCore")
            .foundAtUrl("/" + "TPM" + "/central-core")
            .byUserWithNetId("SYSTEM")
            .assignToFacultyWithId("TPM").constructNodeInstance();
        this.node3 = new NodeBuilder()
            .setNodeCpuResourceCapacityTo(0.0)
            .setNodeGpuResourceCapacityTo(0.0)
            .setNodeMemoryResourceCapacityTo(0.0)
            .withNodeName("FacultyCentralCore")
            .foundAtUrl("/" + "AE" + "/central-core")
            .byUserWithNetId("SYSTEM")
            .assignToFacultyWithId("AE").constructNodeInstance();
        this.job1 = new JobBuilder().preferredCompletedBeforeDate(LocalDate.of(2022, 12, 15))
            .needingMemoryResources(1.0).needingGpuResources(1.0).needingCpuResources(5.0).withDescription("desc")
            .havingName("job").requestedByUserWithNetId("ariel").requestedThroughFaculty("EWI")
            .constructJobInstance();
        this.job1.setScheduledFor(LocalDate.of(2022, 12, 14));
        this.job2 = new JobBuilder().preferredCompletedBeforeDate(LocalDate.of(2022, 12, 15))
            .needingMemoryResources(0.0).needingGpuResources(1.0).needingCpuResources(2.0).withDescription("desc")
            .havingName("ob").requestedByUserWithNetId("ariel").requestedThroughFaculty("AE")
            .constructJobInstance();
        this.job2.setScheduledFor(LocalDate.of(2022, 12, 14));
        this.job3 = new JobBuilder().preferredCompletedBeforeDate(LocalDate.of(2022, 12, 15))
            .needingMemoryResources(1.0).needingGpuResources(1.0).needingCpuResources(2.0).withDescription("desc")
            .havingName("job").requestedByUserWithNetId("ariel").requestedThroughFaculty("EWI")
            .constructJobInstance();
        this.job3.setScheduledFor(LocalDate.of(2022, 12, 14));
        this.job4 = new JobBuilder().preferredCompletedBeforeDate(LocalDate.of(2022, 12, 17))
            .needingMemoryResources(1.0).needingGpuResources(1.0).needingCpuResources(2.0).withDescription("desc")
            .havingName("jb").requestedByUserWithNetId("ariel").requestedThroughFaculty("EWI")
            .constructJobInstance();
        this.job4.setScheduledFor(LocalDate.of(2022, 12, 14));
    }

    @Test
    public void saveNodeTest() {
        this.dataProcessingService.save(this.node1);
        assertThat(this.dataProcessingService.getByUrl(node1.getUrl())).isEqualTo(node1);
    }

    @Test
    public void getNumberOfNodesInRepoTest() {
        assertThat(this.dataProcessingService.getNumberOfNodesInRepository()).isEqualTo(0);

        this.dataProcessingService.save(this.node1);
        assertThat(this.dataProcessingService.getNumberOfNodesInRepository()).isEqualTo(1);
    }

    @Test
    public void nodeNotFoundExistsByUrl() {
        assertThat(this.dataProcessingService.existsByUrl(this.node1.getUrl())).isFalse();
    }

    @Test
    public void nodeExistsByUrl() {
        this.dataProcessingService.save(this.node1);
        assertThat(this.dataProcessingService.existsByUrl(node1.getUrl())).isTrue();
    }

    @Test
    public void nodeNotFoundExistsByFacultyId() {
        assertThat(this.dataProcessingService.existsByFacultyId(this.node1.getFacultyId())).isFalse();
    }

    @Test
    public void nodeExistsByFacultyId() {
        this.dataProcessingService.save(this.node1);
        assertThat(this.dataProcessingService.existsByFacultyId(this.node1.getFacultyId())).isTrue();
    }

    @Test
    public void getByUrlTest() {
        this.dataProcessingService.save(this.node1);
        assertThat(this.dataProcessingService.getByUrl(this.node1.getUrl())).isEqualTo(this.node1);
    }

    @Test
    public void getByFacultyIdTest() {
        List<Node> foundNode = new ArrayList<>();
        foundNode.add(this.node1);
        this.dataProcessingService.save(this.node1);
        assertThat(this.dataProcessingService.getByFacultyId(this.node1.getFacultyId()))
            .isEqualTo(foundNode);
    }

    @Test
    public void getAllNodesEmptyRepoTest() {
        assertThat(this.dataProcessingService.getAllNodes()).isEqualTo(new ArrayList<>());
    }

    @Test
    public void getAllNodesTest() {
        this.dataProcessingService.save(this.node1);
        this.dataProcessingService.save(this.node2);
        this.dataProcessingService.save(this.node3);

        List<Node> nodes = new ArrayList<>();
        nodes.add(this.node1);
        nodes.add(this.node2);
        nodes.add(this.node3);

        assertThat(this.dataProcessingService.getAllNodes()).isEqualTo(nodes);
    }

    @Test
    public void deleteNodeTest() {
        this.dataProcessingService.save(this.node1);
        assertThat(this.dataProcessingService.getNumberOfNodesInRepository()).isEqualTo(1);

        this.dataProcessingService.deleteNode(this.node1);
        assertThat(this.dataProcessingService.getNumberOfNodesInRepository()).isEqualTo(0);
    }

    @Test
    public void deleteAllNodesTest() {
        this.dataProcessingService.save(this.node1);
        this.dataProcessingService.save(this.node2);
        this.dataProcessingService.save(this.node3);

        this.dataProcessingService.deleteAllNodes();
        assertThat(this.dataProcessingService.getNumberOfNodesInRepository()).isEqualTo(0);
    }

    @Test
    public void getAllFacultiesEmptyRepo() {
        assertThat(this.dataProcessingService.getAllFaculties()).isEqualTo(new ArrayList<>());
    }

    @Test
    public void getAllFaculties() {
        this.dataProcessingService.save(this.node1);
        this.dataProcessingService.save(this.node2);

        List<String> faculties = new ArrayList<>();
        faculties.add("EWI");
        faculties.add("TPM");

        assertThat(this.dataProcessingService.getAllFaculties()).isEqualTo(faculties);
    }

    @Test
    public void getAssignedResourcesPerFaculty() {
        this.dataProcessingService.save(this.node1);
        this.dataProcessingService.save(this.node2);
        this.dataProcessingService.save(this.node3);

        List<FacultyTotalResources> facultyResources =
            this.dataProcessingService.getAssignedResourcesPerFaculty();

        assertThat(facultyResources.get(0).getFaculty_Id()).isEqualTo("AE");
        assertThat(facultyResources.get(0).getCpu_Resources()).isEqualTo(0);
    }

    @Test
    public void getAssignedResourcesForFacultyId() {
        this.dataProcessingService.save(this.node1);
        this.dataProcessingService.save(this.node2);
        this.dataProcessingService.save(this.node3);

        FacultyTotalResources facultyResources =
            this.dataProcessingService.getAssignedResourcesForGivenFaculty("EWI");

        assertThat(facultyResources.getFaculty_Id()).isEqualTo("EWI");
        assertThat(facultyResources.getCpu_Resources()).isEqualTo(0);
    }

    @Test
    public void doesntExistInScheduleByFaculty() {
        assertThat(this.dataProcessingService.existsInScheduleByFacultyId("EWI")).isFalse();
    }

    @Test
    public void existInScheduleByFaculty() {
        this.dataProcessingService.saveInSchedule(job1);
        assertThat(this.dataProcessingService.existsInScheduleByFacultyId("EWI")).isTrue();
    }

    @Test
    public void doesntExistInScheduleForGivenDate() {
        LocalDate date = LocalDate.of(2022, 12, 24);
        assertThat(this.dataProcessingService.existsInScheduleByScheduledFor(date)).isFalse();
    }

    @Test
    public void existInScheduleForGivenDate() {
        this.dataProcessingService.saveInSchedule(job1);

        //System.out.println(this.dataProcessingService
        // .getJobRepository().findByFacultyId("EWI").get(0).getScheduledFor());

        LocalDate date = LocalDate.of(2022, 12, 14);
        assertThat(this.dataProcessingService.existsInScheduleByScheduledFor(date)).isTrue();
    }

    @Test
    public void getAllJobsFromScheduleEmpty() {
        assertThat(this.dataProcessingService.getAllJobsFromSchedule()).isEqualTo(new ArrayList<>());
    }

    @Test
    public void getAllJobsFromScheduleFilled() {
        this.dataProcessingService.saveInSchedule(job1);
        this.dataProcessingService.saveInSchedule(job2);

        List<Job> jobs = new ArrayList<>();
        jobs.add(job1);
        jobs.add(job2);

        assertThat(this.dataProcessingService.getAllJobsFromSchedule()).isEqualTo(jobs);
    }

    @Test
    public void getReservedResourcesPerFacultyPerDayTest() {
        this.dataProcessingService.saveInSchedule(job1);
        this.dataProcessingService.saveInSchedule(job2);
        this.dataProcessingService.saveInSchedule(job3);
        this.dataProcessingService.saveInSchedule(job4);

        LocalDate date = LocalDate.of(2022, 12, 14);

        FacultyDatedTotalResources ewi = this.dataProcessingService
            .getReservedResourcesPerFacultyPerDay().get(1);
        assertThat(ewi.getFaculty_Id()).isEqualTo("EWI");
        assertThat(ewi.getScheduled_Date()).isEqualTo(date);
        assertThat(ewi.getCpu_Resources()).isEqualTo(9);
    }

    @Test
    public void getReservedResourcesPerFacultyForaGivenDateTest() {
        this.dataProcessingService.saveInSchedule(job1);
        this.dataProcessingService.saveInSchedule(job2);
        this.dataProcessingService.saveInSchedule(job3);
        this.dataProcessingService.saveInSchedule(job4);

        LocalDate date = LocalDate.of(2022, 12, 14);

        FacultyDatedTotalResources ewi = this.dataProcessingService
            .getReservedResourcesPerFacultyForGivenDay(date).get(1);
        assertThat(ewi.getFaculty_Id()).isEqualTo("EWI");
        assertThat(ewi.getScheduled_Date()).isEqualTo(date);
        assertThat(ewi.getCpu_Resources()).isEqualTo(9);

        FacultyDatedTotalResources ae = this.dataProcessingService
            .getReservedResourcesPerFacultyForGivenDay(date).get(0);
        assertThat(ae.getFaculty_Id()).isEqualTo("AE");
        assertThat(ae.getScheduled_Date()).isEqualTo(date);
        assertThat(ae.getCpu_Resources()).isEqualTo(2);
    }

    @Test
    public void getReservedResourcesPerDayForGivenFaculty() {
        this.dataProcessingService.saveInSchedule(job1);
        this.dataProcessingService.saveInSchedule(job2);
        this.dataProcessingService.saveInSchedule(job3);
        this.dataProcessingService.saveInSchedule(job4);

        LocalDate date1 = LocalDate.of(2022, 12, 14);

        FacultyDatedTotalResources ewi = this.dataProcessingService
            .getReservedResourcesPerDayForGivenFaculty("EWI").get(0);
        assertThat(ewi.getFaculty_Id()).isEqualTo("EWI");
        assertThat(ewi.getScheduled_Date()).isEqualTo(date1);
        assertThat(ewi.getCpu_Resources()).isEqualTo(9);
    }

    @Test
    public void getReservedResourcesForGivenDayForGivenFaculty() {
        this.dataProcessingService.saveInSchedule(job1);
        this.dataProcessingService.saveInSchedule(job2);
        this.dataProcessingService.saveInSchedule(job3);
        this.dataProcessingService.saveInSchedule(job4);

        LocalDate date1 = LocalDate.of(2022, 12, 14);
        LocalDate date2 = LocalDate.of(2022, 12, 15);

        FacultyDatedTotalResources ewi = this.dataProcessingService
            .getReservedResourcesForGivenDayForGivenFaculty(date1, "EWI").get(0);
        assertThat(ewi.getFaculty_Id()).isEqualTo("EWI");
        assertThat(ewi.getScheduled_Date()).isEqualTo(date1);
        assertThat(ewi.getCpu_Resources()).isEqualTo(9);

        assertThat(this.dataProcessingService
            .getReservedResourcesForGivenDayForGivenFaculty(date2, "EWI"))
            .isEqualTo(new ArrayList<>());

        FacultyDatedTotalResources ae = this.dataProcessingService
            .getReservedResourcesForGivenDayForGivenFaculty(date1, "AE").get(0);
        assertThat(ae.getFaculty_Id()).isEqualTo("AE");
        assertThat(ae.getScheduled_Date()).isEqualTo(date1);
        assertThat(ae.getCpu_Resources()).isEqualTo(2);
    }

    @Test
    public void findLatestDateWithReservedResourcesEmptySchedule() {
        assertThat(this.dataProcessingService.findLatestDateWithReservedResources())
            .isEqualTo(dateProvider.getTomorrow());
    }

    @Test
    public void findLatestDateWithReservedResourcesFilledSchedule() {
        this.dataProcessingService.saveInSchedule(job1);
        this.dataProcessingService.saveInSchedule(job2);
        this.dataProcessingService.saveInSchedule(job3);
        this.dataProcessingService.saveInSchedule(job4);

        LocalDate date = LocalDate.of(2022, 12, 14);

        assertThat(this.dataProcessingService.findLatestDateWithReservedResources())
            .isEqualTo(date);
    }



    @Test
    public void getAvailableResourcesForGivenFacultyUntilDayFilledSchedule() {
        this.dateProvider = mock(DateProvider.class);

        when(this.dateProvider.getTomorrow()).thenReturn(LocalDate.of(2022, 12, 23));

        this.node1.setCpuResources(10);
        this.node1.setMemoryResources(10);
        this.node1.setGpuResources(10);
        this.dataProcessingService.save(this.node1);

        LocalDate startDate = LocalDate.of(2022, 12, 24);

        this.job1.setScheduledFor(startDate);
        this.job2.setScheduledFor(startDate);
        this.job3.setScheduledFor(startDate);
        this.job4.setScheduledFor(startDate);
        this.dataProcessingService.saveInSchedule(job1);
        this.dataProcessingService.saveInSchedule(job2);
        this.dataProcessingService.saveInSchedule(job3);
        this.dataProcessingService.saveInSchedule(job4);

        /*Job job = this.dataProcessingService.getJobRepository().findByFacultyId("EWI").get(0);
        System.out.println(job.getScheduledFor().toString());*/

        LocalDate endDate = LocalDate.of(2022, 12, 25);

        List<AvailableResourcesForDate> availableResources =
            this.dataProcessingService.getAvailableResourcesForGivenFacultyUntilDay("EWI", endDate);

        assertThat(availableResources.get(0).getAvailableCpu()).isEqualTo(10);
        assertThat(availableResources.get(1).getAvailableCpu()).isEqualTo(1);
        assertThat(availableResources.get(2).getAvailableCpu()).isEqualTo(10);
    }

    @Test
    public void getAvailableResourcesForGivenFacultyUntilDayEmptySchedule() {
        this.dateProvider = mock(DateProvider.class);

        when(this.dateProvider.getTomorrow()).thenReturn(LocalDate.of(2022, 12, 23));

        this.node3.setCpuResources(10);
        this.node3.setMemoryResources(10);
        this.node3.setGpuResources(10);
        this.dataProcessingService.save(this.node3);

        LocalDate startDate = LocalDate.of(2022, 12, 24);

        this.job3.setScheduledFor(startDate);
        this.job4.setScheduledFor(startDate);
        this.dataProcessingService.saveInSchedule(job3);
        this.dataProcessingService.saveInSchedule(job4);
        /*Job job = this.dataProcessingService.getJobRepository().findByFacultyId("EWI").get(0);
        System.out.println(job.getScheduledFor().toString());*/

        LocalDate endDate = LocalDate.of(2022, 12, 25);

        List<AvailableResourcesForDate> availableResources =
            this.dataProcessingService.getAvailableResourcesForGivenFacultyUntilDay("AE", endDate);

        assertThat(availableResources.get(0).getAvailableCpu()).isEqualTo(10);
        assertThat(availableResources.get(1).getAvailableCpu()).isEqualTo(10);
        assertThat(availableResources.get(2).getAvailableCpu()).isEqualTo(10);
    }

    @Test
    public void getAvailableResourcesForAllFacultiesForAllDaysEmptySchedule() {
        assertThat(this.dataProcessingService
            .getAvailableResourcesForAllFacultiesForAllDays()).isEqualTo(new ArrayList<>());
    }

    @Test
    public void getAvailableResourcesForAllFacultiesForAllDaysFilledSchedule() {
        LocalDate startDate = LocalDate.of(2022, 12, 23);
        this.dateProvider = mock(DateProvider.class);
        when(this.dateProvider.getTomorrow()).thenReturn(startDate);

        this.node3.setCpuResources(10);
        this.node3.setMemoryResources(10);
        this.node3.setGpuResources(10);
        this.dataProcessingService.save(this.node3);

        this.node1.setCpuResources(10);
        this.node1.setMemoryResources(10);
        this.node1.setGpuResources(10);
        this.dataProcessingService.save(this.node1);

        this.job1.setScheduledFor(startDate);
        this.job2.setScheduledFor(startDate);
        this.job3.setScheduledFor(startDate);
        this.job4.setScheduledFor(startDate);
        this.dataProcessingService.saveInSchedule(this.job1);
        this.dataProcessingService.saveInSchedule(this.job2);
        this.dataProcessingService.saveInSchedule(this.job3);
        this.dataProcessingService.saveInSchedule(this.job4);

        assertThat(this.dataProcessingService
            .getAvailableResourcesForAllFacultiesForAllDays().get(0).getFacultyId()).isEqualTo("AE");
        assertThat(this.dataProcessingService
            .getAvailableResourcesForAllFacultiesForAllDays().get(0).getDate()).isEqualTo(startDate);
        assertThat(this.dataProcessingService
            .getAvailableResourcesForAllFacultiesForAllDays().get(0).getTotalCpu())
            .isEqualTo(8);
    }

    @Test
    public void getAvailableResourcesForGivenFacultyForAllDaysTest() {
        this.dataProcessingService.save(this.node1);

        assertThat(this.dataProcessingService
            .getAvailableResourcesForGivenFacultyForAllDays("EWI").get(0).getFacultyId())
            .isEqualTo("EWI");

        assertThat(this.dataProcessingService
            .getAvailableResourcesForGivenFacultyForAllDays("EWI").get(0).getTotalCpu())
            .isEqualTo(0);
    }

    @Test
    public void getAvailableResourcesForAllFacultiesForGivenDayNoNodesPosted() {
        this.node3.setCpuResources(10);
        this.node3.setMemoryResources(10);
        this.node3.setGpuResources(10);
        this.dataProcessingService.save(this.node3);

        this.node1.setCpuResources(10);
        this.node1.setMemoryResources(10);
        this.node1.setGpuResources(10);
        this.dataProcessingService.save(this.node1);

        LocalDate lookupDate = LocalDate.of(2022, 12, 16);

        assertThat(this.dataProcessingService
            .getAvailableResourcesForAllFacultiesForGivenDay(lookupDate).get(0).getTotalCpu())
            .isEqualTo(10);
        assertThat(this.dataProcessingService
            .getAvailableResourcesForAllFacultiesForGivenDay(lookupDate).get(1).getTotalCpu())
            .isEqualTo(10);
    }

    @Test
    public void getAvailableResourcesForAllFacultiesForGivenDayScheduleFilled() {
        this.node3.setCpuResources(10);
        this.node3.setMemoryResources(10);
        this.node3.setGpuResources(10);
        this.dataProcessingService.save(this.node3);

        this.node1.setCpuResources(10);
        this.node1.setMemoryResources(10);
        this.node1.setGpuResources(10);
        this.dataProcessingService.save(this.node1);

        LocalDate lookupDate = LocalDate.of(2022, 12, 23);

        this.job1.setScheduledFor(lookupDate);
        this.dataProcessingService.saveInSchedule(this.job1);

        assertThat(this.dataProcessingService
            .getAvailableResourcesForAllFacultiesForGivenDay(lookupDate).get(0).getTotalCpu())
            .isEqualTo(10);
        assertThat(this.dataProcessingService
            .getAvailableResourcesForAllFacultiesForGivenDay(lookupDate).get(1).getTotalCpu())
            .isEqualTo(5);
    }

    @Test
    public void getAvailableResourcesForGivenFacultyForGivenDayScheduleFilled() {
        this.node3.setCpuResources(10);
        this.node3.setMemoryResources(10);
        this.node3.setGpuResources(10);
        this.dataProcessingService.save(this.node3);

        this.node1.setCpuResources(10);
        this.node1.setMemoryResources(10);
        this.node1.setGpuResources(10);
        this.dataProcessingService.save(this.node1);

        LocalDate lookupDate = LocalDate.of(2023, 1, 26);

        this.job1.setScheduledFor(lookupDate);
        this.dataProcessingService.saveInSchedule(this.job1);

        assertThat(this.dataProcessingService
            .getAvailableResourcesForGivenFacultyForGivenDay(lookupDate, "EWI")
            .get(0).getTotalCpu()).isEqualTo(5);
    }

}
