package nl.tudelft.sem.template.cluster.strategies;

import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.template.cluster.domain.providers.implementations.RandomNumberProvider;
import nl.tudelft.sem.template.cluster.domain.strategies.AssignNodeToRandomFacultyStrategy;
import nl.tudelft.sem.template.cluster.models.FacultyResourcesResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles({"test", "mockRandomNumberProvider"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RandomFacultyStrategyTest {

    @Autowired
    private transient RandomNumberProvider mockRandomNumberProvider;

    private AssignNodeToRandomFacultyStrategy strat;

    private List<FacultyResourcesResponseModel> faculties;

    @BeforeEach
    public void setup() {
        when(mockRandomNumberProvider.betweenZeroAndSpecified(5)).thenReturn(3);
        when(mockRandomNumberProvider.betweenZeroAndSpecified(2)).thenReturn(1);
        when(mockRandomNumberProvider.betweenZeroAndSpecified(4)).thenReturn(0);
        strat = new AssignNodeToRandomFacultyStrategy(mockRandomNumberProvider);
        faculties = new ArrayList<>();
        faculties.add(new FacultyResourcesResponseModel("EWI", 2.0, 3.0, 5.0));
        faculties.add(new FacultyResourcesResponseModel("AE", 0.0, 1.0, 0.0));
        faculties.add(new FacultyResourcesResponseModel("TPM", 3.0, 1.0, 3.0));
        faculties.add(new FacultyResourcesResponseModel("IO", 2.0, 0.0, 1.0));
        faculties.add(new FacultyResourcesResponseModel("CEG", 7.0, 5.0, 2.0));
    }

    @Test
    public void noFacultiesTest() {
        assertThatThrownBy(() -> strat.pickFacultyToAssignNodeTo(new ArrayList<>()))
                .isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessageContaining("Index 0 out of bounds for length 0");
    }

    @Test
    public void fiveDifferentFacultiesTest() {
        assertThat(strat.pickFacultyToAssignNodeTo(faculties)).isEqualTo("IO");
    }

    @Test
    public void fourDifferentFacultiesTest() {
        faculties.remove(new FacultyResourcesResponseModel("IO", 2.0, 0.0, 1.0));
        assertThat(strat.pickFacultyToAssignNodeTo(faculties)).isEqualTo("EWI");
    }

    @Test
    public void twoIdenticalFacultiesTest() {
        faculties.remove(new FacultyResourcesResponseModel("EWI", 2.0, 3.0, 5.0));
        faculties.remove(new FacultyResourcesResponseModel("AE", 0.0, 1.0, 0.0));
        faculties.remove(new FacultyResourcesResponseModel("CEG", 7.0, 5.0, 2.0));
        faculties.add(new FacultyResourcesResponseModel("CEG", 3.0, 1.0, 3.0));
        assertThat(strat.pickFacultyToAssignNodeTo(faculties)).isEqualTo("TPM");
    }

}
