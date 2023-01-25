package nl.tudelft.sem.template.cluster.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import nl.tudelft.sem.template.cluster.domain.builders.JobBuilder;
import nl.tudelft.sem.template.cluster.domain.builders.NodeBuilder;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.providers.DateProvider;
import nl.tudelft.sem.template.cluster.domain.services.JobReschedulingService;
import nl.tudelft.sem.template.cluster.domain.services.JobSchedulingService;
import nl.tudelft.sem.template.cluster.models.FacultyDatedResourcesResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class JobReschedulingServiceTest {

    @Autowired
    private transient JobSchedulingService jobSchedulingService;

    @Autowired
    private transient JobReschedulingService jobReschedulingService;
    @Autowired
    private transient DateProvider dateProvider;

    private Node node1;
    private Node node2;

    private Job job1;
    private Job job2;

    @BeforeEach
    void setUp() {
        this.jobSchedulingService.getSchedulingDataProcessingService().deleteAllJobsScheduled();
        this.jobSchedulingService.getNodeDataProcessingService().deleteAllNodes();

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
                .setNodeCpuResourceCapacityTo(10.0)
                .setNodeGpuResourceCapacityTo(10.0)
                .setNodeMemoryResourceCapacityTo(10.0)
                .withNodeName("FacultyCentralCore")
                .foundAtUrl("/" + "AE" + "/central-core")
                .byUserWithNetId("SYSTEM")
                .assignToFacultyWithId("AE").constructNodeInstance();
        this.job1 = new JobBuilder().preferredCompletedBeforeDate(dateProvider.getTomorrow())
                .needingMemoryResources(1.0).needingGpuResources(1.0).needingCpuResources(5.0).withDescription("desc")
                .havingName("job").requestedByUserWithNetId("ariel").requestedThroughFaculty("EWI")
                .constructJobInstance();
        this.job1.setScheduledFor(LocalDate.of(2022, 12, 14));
        this.job2 = new JobBuilder().preferredCompletedBeforeDate(dateProvider.getTomorrow())
                .needingMemoryResources(1.0).needingGpuResources(1.0).needingCpuResources(2.0).withDescription("desc")
                .havingName("ob").requestedByUserWithNetId("ariel").requestedThroughFaculty("AE")
                .constructJobInstance();
        this.job2.setScheduledFor(LocalDate.of(2022, 12, 14));
    }

    @Test
    public void simpleReschedulingWithoutDroppingTest() {
        this.jobSchedulingService.getSchedulingDataProcessingService().deleteAllJobsScheduled();
        this.jobSchedulingService.getNodeDataProcessingService().deleteAllNodes();

        this.node1.setCpuResources(5);
        this.node1.setMemoryResources(4);
        this.node1.setGpuResources(3);
        this.node1.setFacultyId("AE");

        this.jobSchedulingService.getNodeDataProcessingService().save(this.node1);

        this.node2.setCpuResources(3);
        this.node2.setMemoryResources(2);
        this.node2.setGpuResources(1);
        this.node2.setFacultyId("AE");

        this.jobSchedulingService.getNodeDataProcessingService().save(this.node2);

        this.job1.setRequiredGpu(4.0);
        this.job1.setRequiredMemory(5.0);
        this.job1.setFacultyId("AE");
        this.job1.setScheduledFor(dateProvider.getTomorrow());

        this.jobSchedulingService.getSchedulingDataProcessingService().saveInSchedule(this.job1);

        this.job2.setRequiredCpu(4.0);
        this.job2.setRequiredGpu(2.0);
        this.job2.setRequiredMemory(2.0);
        this.job2.setFacultyId("AE");
        this.job2.setScheduledFor(dateProvider.getTomorrow());

        this.jobSchedulingService.getSchedulingDataProcessingService().saveInSchedule(this.job2);

        // we pretend that a node has been removed. There is too many reservations, and they should be rescheduled.
        this.jobReschedulingService.rescheduleJobsForFacultiesWithRemovedNodes(List.of("AE"));

        assertThat(this.jobSchedulingService.getSchedulingDataProcessingService()
                .findLatestDateWithReservedResources().equals(dateProvider.getTomorrow())).isFalse();
        assertThat(this.jobSchedulingService.getSchedulingDataProcessingService()
                .getAvailableResourcesForGivenFacultyForGivenDay(dateProvider.getTomorrow(), "AE"))
                .isEqualTo(List.of(new FacultyDatedResourcesResponseModel(dateProvider.getTomorrow(), "AE", 4.0, 2.0, 4.0)));
    }

    @Test
    public void simpleReschedulingWithoutDroppingEnoughCpuTest() {
        this.jobSchedulingService.getSchedulingDataProcessingService().deleteAllJobsScheduled();
        this.jobSchedulingService.getNodeDataProcessingService().deleteAllNodes();

        this.node1.setCpuResources(5);
        this.node1.setMemoryResources(4);
        this.node1.setGpuResources(3);
        this.node1.setFacultyId("AE");

        this.jobSchedulingService.getNodeDataProcessingService().save(this.node1);

        this.node2.setCpuResources(3);
        this.node2.setMemoryResources(2);
        this.node2.setGpuResources(1);
        this.node2.setFacultyId("AE");

        this.jobSchedulingService.getNodeDataProcessingService().save(this.node2);

        this.job1.setRequiredCpu(3.0);
        this.job1.setRequiredGpu(4.0);
        this.job1.setRequiredMemory(5.0);
        this.job1.setFacultyId("AE");
        this.job1.setScheduledFor(dateProvider.getTomorrow());

        this.jobSchedulingService.getSchedulingDataProcessingService().saveInSchedule(this.job1);

        this.job2.setRequiredCpu(4.0);
        this.job2.setRequiredGpu(2.0);
        this.job2.setRequiredMemory(2.0);
        this.job2.setFacultyId("AE");
        this.job2.setScheduledFor(dateProvider.getTomorrow());

        this.jobSchedulingService.getSchedulingDataProcessingService().saveInSchedule(this.job2);

        // we pretend that a node has been removed. There is too many reservations, and they should be rescheduled.
        this.jobReschedulingService.rescheduleJobsForFacultiesWithRemovedNodes(List.of("AE"));

        assertThat(this.jobSchedulingService.getSchedulingDataProcessingService()
                .findLatestDateWithReservedResources().equals(dateProvider.getTomorrow())).isFalse();
        assertThat(this.jobSchedulingService.getSchedulingDataProcessingService()
                .getAvailableResourcesForGivenFacultyForGivenDay(dateProvider.getTomorrow(), "AE"))
                .isEqualTo(List.of(new FacultyDatedResourcesResponseModel(dateProvider.getTomorrow(), "AE", 4.0, 2.0, 4.0)));
    }

    @Test
    public void droppingTest() {
        this.jobSchedulingService.getSchedulingDataProcessingService().deleteAllJobsScheduled();
        this.jobSchedulingService.getNodeDataProcessingService().deleteAllNodes();

        this.node1.setFacultyId("AE");
        this.jobSchedulingService.getNodeDataProcessingService().save(node1);

        this.job1.setRequiredCpu(3.0);
        this.job1.setRequiredGpu(4.0);
        this.job1.setRequiredMemory(5.0);
        this.job1.setFacultyId("AE");
        this.job1.setScheduledFor(dateProvider.getTomorrow().plusDays(3));

        this.jobSchedulingService.getSchedulingDataProcessingService().saveInSchedule(this.job1);

        this.jobReschedulingService.rescheduleJobsForFacultiesWithRemovedNodes(List.of("AE"));

        assertThat(this.jobSchedulingService.getSchedulingDataProcessingService()
                .findLatestDateWithReservedResources().equals(dateProvider.getTomorrow())).isTrue();
        assertThat(this.jobSchedulingService.getSchedulingDataProcessingService()
                .existsInScheduleByFacultyId("AE")).isFalse();
    }

    @Test
    public void missingMemoryReschedulingTest() {
        this.jobSchedulingService.getSchedulingDataProcessingService().deleteAllJobsScheduled();
        this.jobSchedulingService.getNodeDataProcessingService().deleteAllNodes();

        this.node1.setCpuResources(5);
        this.node1.setMemoryResources(4);
        this.node1.setGpuResources(3);
        this.node1.setFacultyId("AE");

        this.jobSchedulingService.getNodeDataProcessingService().save(this.node1);

        this.node2.setCpuResources(3);
        this.node2.setMemoryResources(2);
        this.node2.setGpuResources(1);
        this.node2.setFacultyId("AE");

        this.jobSchedulingService.getNodeDataProcessingService().save(this.node2);

        this.job1.setRequiredCpu(3.0);
        this.job1.setRequiredGpu(1.0);
        this.job1.setRequiredMemory(5.0);
        this.job1.setFacultyId("AE");
        this.job1.setScheduledFor(dateProvider.getTomorrow());

        this.jobSchedulingService.getSchedulingDataProcessingService().saveInSchedule(this.job1);

        this.job2.setRequiredCpu(4.0);
        this.job2.setRequiredGpu(2.0);
        this.job2.setRequiredMemory(2.0);
        this.job2.setFacultyId("AE");
        this.job2.setScheduledFor(dateProvider.getTomorrow());

        this.jobSchedulingService.getSchedulingDataProcessingService().saveInSchedule(this.job2);

        // we pretend that a node has been removed. There is too many reservations, and they should be rescheduled.
        this.jobReschedulingService.rescheduleJobsForFacultiesWithRemovedNodes(List.of("AE"));

        assertThat(this.jobSchedulingService.getSchedulingDataProcessingService()
                .findLatestDateWithReservedResources().equals(dateProvider.getTomorrow())).isFalse();
        assertThat(this.jobSchedulingService.getSchedulingDataProcessingService()
                .getAvailableResourcesForGivenFacultyForGivenDay(dateProvider.getTomorrow(), "AE"))
                .isEqualTo(List.of(new FacultyDatedResourcesResponseModel(dateProvider.getTomorrow(), "AE", 4.0, 2.0, 4.0)));

    }
}
