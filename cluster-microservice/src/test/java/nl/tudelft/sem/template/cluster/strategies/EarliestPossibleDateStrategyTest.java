package nl.tudelft.sem.template.cluster.strategies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.template.cluster.domain.builders.JobBuilder;
import nl.tudelft.sem.template.cluster.domain.cluster.AvailableResourcesForDate;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;
import nl.tudelft.sem.template.cluster.domain.providers.DateProvider;
import nl.tudelft.sem.template.cluster.domain.strategies.EarliestPossibleDateStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class EarliestPossibleDateStrategyTest {

    @Autowired
    private transient DateProvider dateProvider;

    private EarliestPossibleDateStrategy strat;
    private List<AvailableResourcesForDate> resources;
    private Job job;

    @BeforeEach
    public void setup() {
        strat = new EarliestPossibleDateStrategy();

        resources = new ArrayList<AvailableResourcesForDate>();
        resources.add(new AvailableResourcesForDate(dateProvider.getTomorrow(), 3.0, 1.0, 2.0));
        resources.add(new AvailableResourcesForDate(dateProvider.getTomorrow().plusDays(1), 3.0, 4.0, 4.0));
        resources.add(new AvailableResourcesForDate(dateProvider.getTomorrow().plusDays(2), 5.0, 1.0, 0.0));
        job = new JobBuilder().requestedThroughFaculty("EWI")
                .requestedByUserWithNetId("Alan")
                .havingName("Job")
                .withDescription("desc")
                .needingCpuResources(1.0)
                .needingGpuResources(1.0)
                .needingMemoryResources(0.0)
                .preferredCompletedBeforeDate(dateProvider.getTomorrow().plusDays(2))
                .constructJobInstance();

    }

    @Test
    public void emptyListTest() {
        assertThatThrownBy(() -> strat.scheduleJobFor(new ArrayList<>(), job))
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessageContaining("Index -1 out of bounds for length 0");
    }

    @Test
    public void validJobTest() {
        assertThat(strat.scheduleJobFor(resources, job)).isEqualTo(dateProvider.getTomorrow());
    }

    @Test
    public void tooLittleCpuTest() {
        job.setRequiredCpu(5.0);
        assertThat(strat.scheduleJobFor(resources, job)).isEqualTo(dateProvider.getTomorrow().plusDays(2));
        job.setRequiredCpu(6.0);
        assertThat(strat.scheduleJobFor(resources, job)).isEqualTo(dateProvider.getTomorrow().plusDays(2));
    }

    @Test
    public void tooLittleGpuTest() {
        job.setRequiredGpu(4.0);
        assertThat(strat.scheduleJobFor(resources, job)).isEqualTo(dateProvider.getTomorrow().plusDays(1));
    }

    @Test
    public void tooLittleMemoryTest() {
        job.setRequiredMemory(4.0);
        assertThat(strat.scheduleJobFor(resources, job)).isEqualTo(dateProvider.getTomorrow().plusDays(1));
    }

}
