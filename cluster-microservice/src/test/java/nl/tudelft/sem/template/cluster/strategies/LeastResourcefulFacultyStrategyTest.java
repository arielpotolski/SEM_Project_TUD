package nl.tudelft.sem.template.cluster.strategies;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.template.cluster.domain.strategies.AssignNodeToLeastResourcefulFacultyStrategy;
import nl.tudelft.sem.template.cluster.models.FacultyResourcesResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class LeastResourcefulFacultyStrategyTest {

    private AssignNodeToLeastResourcefulFacultyStrategy strat;

    private List<FacultyResourcesResponseModel> faculties;

    /**
     * Setup for the tests.
     */
    @BeforeEach
    public void setup() {
        strat = new AssignNodeToLeastResourcefulFacultyStrategy();
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
    public void facultiesTest1() {
        assertThat(strat.pickFacultyToAssignNodeTo(faculties)).isEqualTo("AE");
    }

    @Test
    public void facultiesTest2() {
        faculties.remove(1);
        assertThat(strat.pickFacultyToAssignNodeTo(faculties)).isEqualTo("IO");
    }

    @Test
    public void facultiesTest3() {
        faculties.remove(1);
        faculties.remove(2);
        assertThat(strat.pickFacultyToAssignNodeTo(faculties)).isEqualTo("TPM");
    }

}
