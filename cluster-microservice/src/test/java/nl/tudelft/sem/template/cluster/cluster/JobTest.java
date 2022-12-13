package nl.tudelft.sem.template.cluster.cluster;

import nl.tudelft.sem.template.cluster.domain.cluster.Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

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
    void differentCpuEqualsTest() {
        LocalDate date = LocalDate.of(2022, 12, 24);
        Job j = new Job("1", "1", "name1", "desc1",
            3, 1, 1, date);

        assertThat(this.job.equals(j)).isFalse();
    }

    @Test
    void differentGpuEqualsTest() {
        LocalDate date = LocalDate.of(2022, 12, 24);
        Job j = new Job("1", "1", "name1", "desc1",
            2, 2, 1, date);

        assertThat(this.job.equals(j)).isFalse();
    }

    @Test
    void differentMemoryEqualsTest() {
        LocalDate date = LocalDate.of(2022, 12, 24);
        Job j = new Job("1", "1", "name1", "desc1",
            2, 1, 2, date);

        assertThat(this.job.equals(j)).isFalse();
    }

    @Test
    void differentFacultyIdEqualsTest() {
        LocalDate date = LocalDate.of(2022, 12, 24);
        Job j = new Job("2", "1", "name1", "desc1",
            2, 1, 1, date);

        assertThat(this.job.equals(j)).isFalse();
    }

    @Test
    void differentUserNetIdEqualsTest() {
        LocalDate date = LocalDate.of(2022, 12, 24);
        Job j = new Job("1", "2", "name1", "desc1",
            2, 1, 1, date);

        assertThat(this.job.equals(j)).isFalse();
    }

    @Test
    void differentNameEqualsTest() {
        LocalDate date = LocalDate.of(2022, 12, 24);
        Job j = new Job("1", "1", "name2", "desc1",
            2, 1, 1, date);

        assertThat(this.job.equals(j)).isFalse();
    }

    @Test
    void differentDescriptionEqualsTest() {
        LocalDate date = LocalDate.of(2022, 12, 24);
        Job j = new Job("1", "1", "name1", "desc2",
            2, 1, 1, date);

        assertThat(this.job.equals(j)).isFalse();
    }

    @Test
    void differentPrefDateEqualsTest() {
        LocalDate date = LocalDate.of(2022, 12, 25);
        Job j = new Job("1", "1", "name1", "desc1",
            2, 1, 1, date);

        assertThat(this.job.equals(j)).isFalse();
    }

    @Test
    void equalsTestIsTrue() {
        LocalDate date = LocalDate.of(2022, 12, 24);
        Job j = new Job("1", "1", "name1", "desc1",
            2, 1, 1, date);

        assertThat(this.job.equals(j)).isTrue();
    }

    @Test
    void hashCodeIsDifferent() {
        LocalDate date = LocalDate.of(2022, 12, 24);
        Job j = new Job("1", "1", "name1", "desc1",
            3, 1, 1, date);

        assertThat(this.job.hashCode()).isNotEqualTo(j.hashCode());
    }

    @Test
    void hashCodeIsEqual() {
        LocalDate date = LocalDate.of(2022, 12, 24);
        Job j = new Job("1", "1", "name1", "desc1",
            2, 1, 1, date);

        assertThat(this.job.hashCode()).isEqualTo(j.hashCode());
    }
}
