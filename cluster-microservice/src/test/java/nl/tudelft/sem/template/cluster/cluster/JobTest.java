package nl.tudelft.sem.template.cluster.cluster;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import nl.tudelft.sem.template.cluster.domain.cluster.Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JobTest {

    private Job job;

    @BeforeEach
    void setup() {
        LocalDate date = LocalDate.of(2022, 12, 24);
        this.job = new Job("1", "1", "name1", "desc1",
            2, 1, 1, date);
    }

    @Test
    void constructorTest() {
        assertThat(this.job).isNotNull();
        assertThat(this.job.getRequiredCpu()).isEqualTo(2);
    }

    @Test
    public void resourcesRequestedAreNotValidGpu() {
        this.job.setRequiredGpu(2.1);
        assertThat(this.job.areResourcesNeededValid()).isFalse();
    }

    @Test
    public void resourcesRequestedAreNotValidMemory() {
        this.job.setRequiredMemory(2.1);
        assertThat(this.job.areResourcesNeededValid()).isFalse();
    }

    @Test
    public void resourcesRequestedAreValid() {
        assertThat(this.job.areResourcesNeededValid()).isTrue();
    }

    @Test
    public void equalsNullObjectReturnsFalse() {
        assertThat(this.job.equals(null)).isFalse();
    }

    @Test
    public void equalsObjectOfOtherClassReturnsFalse() {
        assertThat(this.job.equals(1)).isFalse();
    }

    @Test
    public void equalsSameObjectReturnsTrue() {
        Job that = this.job;
        assertThat(this.job.equals(that)).isTrue();
    }

    @Test
    public void differentCpuEqualsTest() {
        LocalDate date = LocalDate.of(2022, 12, 24);
        Job j = new Job("1", "1", "name1", "desc1",
            3, 1, 1, date);

        assertThat(this.job.equals(j)).isFalse();
    }

    @Test
    public void differentGpuEqualsTest() {
        LocalDate date = LocalDate.of(2022, 12, 24);
        Job j = new Job("1", "1", "name1", "desc1",
            2, 2, 1, date);

        assertThat(this.job.equals(j)).isFalse();
    }

    @Test
    public void differentMemoryEqualsTest() {
        LocalDate date = LocalDate.of(2022, 12, 24);
        Job j = new Job("1", "1", "name1", "desc1",
            2, 1, 2, date);

        assertThat(this.job.equals(j)).isFalse();
    }

    @Test
    public void differentFacultyIdEqualsTest() {
        LocalDate date = LocalDate.of(2022, 12, 24);
        Job j = new Job("2", "1", "name1", "desc1",
            2, 1, 1, date);

        assertThat(this.job.equals(j)).isFalse();
    }

    @Test
    public void differentUserNetIdEqualsTest() {
        LocalDate date = LocalDate.of(2022, 12, 24);
        Job j = new Job("1", "2", "name1", "desc1",
            2, 1, 1, date);

        assertThat(this.job.equals(j)).isFalse();
    }

    @Test
    public void differentNameEqualsTest() {
        LocalDate date = LocalDate.of(2022, 12, 24);
        Job j = new Job("1", "1", "name2", "desc1",
            2, 1, 1, date);

        assertThat(this.job.equals(j)).isFalse();
    }

    @Test
    public void differentDescriptionEqualsTest() {
        LocalDate date = LocalDate.of(2022, 12, 24);
        Job j = new Job("1", "1", "name1", "desc2",
            2, 1, 1, date);

        assertThat(this.job.equals(j)).isFalse();
    }

    @Test
    public void differentPrefDateEqualsTest() {
        LocalDate date = LocalDate.of(2022, 12, 25);
        Job j = new Job("1", "1", "name1", "desc1",
            2, 1, 1, date);

        assertThat(this.job.equals(j)).isFalse();
    }

    @Test
    public void equalsTestIsTrue() {
        LocalDate date = LocalDate.of(2022, 12, 24);
        Job j = new Job("1", "1", "name1", "desc1",
            2, 1, 1, date);

        assertThat(this.job.equals(j)).isTrue();
    }

    @Test
    public void hashCodeIsDifferent() {
        LocalDate date = LocalDate.of(2022, 12, 24);
        Job j = new Job("1", "1", "name1", "desc1",
            3, 1, 1, date);

        assertThat(this.job.hashCode()).isNotEqualTo(j.hashCode());
    }

    @Test
    public void hashCodeIsEqual() {
        LocalDate date = LocalDate.of(2022, 12, 24);
        Job j = new Job("1", "1", "name1", "desc1",
            2, 1, 1, date);

        assertThat(this.job.hashCode()).isEqualTo(j.hashCode());
    }
}
