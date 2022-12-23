package nl.tudelft.sem.template.cluster.services;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import nl.tudelft.sem.template.cluster.domain.builders.JobBuilder;
import nl.tudelft.sem.template.cluster.domain.builders.NodeBuilder;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;
import nl.tudelft.sem.template.cluster.domain.cluster.Node;
import nl.tudelft.sem.template.cluster.domain.services.JobSchedulingService;
import nl.tudelft.sem.template.cluster.domain.strategies.LatestAcceptableDateStrategy;
import nl.tudelft.sem.template.cluster.domain.strategies.LeastBusyDateStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class JobSchedulingServiceTest {

    @Autowired
    private transient JobSchedulingService jobSchedulingService;

    private Node node1;
    private Node node2;

    private Job job1;
    private Job job2;

    @BeforeEach
    void setUp() {
        this.jobSchedulingService.getDataProcessingService().deleteAllJobsScheduled();
        this.jobSchedulingService.getDataProcessingService().deleteAllNodes();

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
        this.job1 = new JobBuilder().preferredCompletedBeforeDate(LocalDate.of(2023, 1, 26))
            .needingMemoryResources(1.0).needingGpuResources(1.0).needingCpuResources(5.0).withDescription("desc")
            .havingName("job").requestedByUserWithNetId("ariel").requestedThroughFaculty("EWI")
            .constructJobInstance();
        this.job1.setScheduledFor(LocalDate.of(2022, 12, 14));
        this.job2 = new JobBuilder().preferredCompletedBeforeDate(LocalDate.of(2023, 1, 26))
            .needingMemoryResources(1.0).needingGpuResources(1.0).needingCpuResources(2.0).withDescription("desc")
            .havingName("ob").requestedByUserWithNetId("ariel").requestedThroughFaculty("AE")
            .constructJobInstance();
        this.job2.setScheduledFor(LocalDate.of(2022, 12, 14));
    }

    @Test
    public void changeSchedulingStrategyTest() {
        assertThat(this.jobSchedulingService.getStrategy()).isInstanceOf(LeastBusyDateStrategy.class);
        this.jobSchedulingService.changeSchedulingStrategy(new LatestAcceptableDateStrategy());
        assertThat(this.jobSchedulingService.getStrategy()).isInstanceOf(LatestAcceptableDateStrategy.class);
    }

    @Test
    public void checkIfJobCanBeScheduledFalse() {
        assertThat(this.jobSchedulingService.checkIfJobCanBeScheduled(this.job1)).isFalse();
    }

    @Test
    public void checkIfJobCanBeScheduledNotEnoughCpu() {
        this.node1.setMemoryResources(10);
        this.node1.setGpuResources(10);
        this.jobSchedulingService.getDataProcessingService().save(this.node1);

        assertThat(this.jobSchedulingService.checkIfJobCanBeScheduled(this.job1)).isFalse();
    }

    @Test
    public void checkIfJobCanBeScheduledNotEnoughGpu() {
        this.node1.setMemoryResources(10);
        this.node1.setCpuResources(10);
        this.jobSchedulingService.getDataProcessingService().save(this.node1);

        assertThat(this.jobSchedulingService.checkIfJobCanBeScheduled(this.job1)).isFalse();
    }

    @Test
    public void checkIfJobCanBeScheduledNotEnoughMemory() {
        this.node1.setCpuResources(10);
        this.node1.setGpuResources(10);
        this.jobSchedulingService.getDataProcessingService().save(this.node1);

        assertThat(this.jobSchedulingService.checkIfJobCanBeScheduled(this.job1)).isFalse();
    }

    @Test
    public void checkIfJobCanBeScheduledTrue() {
        this.node1.setGpuResources(10);
        this.node1.setMemoryResources(10);
        this.node1.setCpuResources(10);
        this.jobSchedulingService.getDataProcessingService().save(this.node1);

        assertThat(this.jobSchedulingService.checkIfJobCanBeScheduled(this.job1)).isTrue();
    }

    @Test
    public void scheduleJobTestSuccessful() {
        this.node1.setGpuResources(10);
        this.node1.setMemoryResources(10);
        this.node1.setCpuResources(10);
        this.jobSchedulingService.getDataProcessingService().save(this.node1);

        this.jobSchedulingService.scheduleJob(this.job1);
        assertThat(this.jobSchedulingService.getDataProcessingService()
            .existsInScheduleByFacultyId(this.job1.getFacultyId())).isTrue();
    }

    @Test
    public void scheduleJobTestFails() {
        this.node1.setGpuResources(1);
        this.node1.setMemoryResources(1);
        this.node1.setCpuResources(1);
        this.jobSchedulingService.getDataProcessingService().save(this.node1);

        this.jobSchedulingService.scheduleJob(this.job1);
        assertThat(this.jobSchedulingService.getDataProcessingService()
            .existsInScheduleByFacultyId(this.job1.getFacultyId())).isFalse();
    }

    @Test
    public void multipleJobsScheduledSuccessfully() {
        this.node1.setGpuResources(10);
        this.node1.setMemoryResources(10);
        this.node1.setCpuResources(10);
        this.jobSchedulingService.getDataProcessingService().save(this.node1);

        this.jobSchedulingService.scheduleJob(this.job1);
        assertThat(this.jobSchedulingService.getDataProcessingService()
            .existsInScheduleByFacultyId(this.job1.getFacultyId())).isTrue();

        this.jobSchedulingService.getDataProcessingService().save(this.node2);

        this.jobSchedulingService.scheduleJob(this.job2);
        assertThat(this.jobSchedulingService.getDataProcessingService()
            .existsInScheduleByFacultyId(this.job2.getFacultyId())).isTrue();
    }
}
