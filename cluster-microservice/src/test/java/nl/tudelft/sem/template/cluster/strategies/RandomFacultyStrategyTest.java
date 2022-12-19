package nl.tudelft.sem.template.cluster.strategies;

import java.util.List;
import nl.tudelft.sem.template.cluster.domain.cluster.FacultyTotalResources;
import nl.tudelft.sem.template.cluster.domain.providers.implementations.RandomNumberProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles({"test", "mockRandomNumberProvider"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RandomFacultyStrategyTest {

    @Autowired
    private transient RandomNumberProvider mockRandomNumberProvider;

    private List<FacultyTotalResources> faculties;

    @BeforeEach
    public void setup() {
        when(mockRandomNumberProvider.betweenZeroAndSpecified(5)).thenReturn(3);
        when(mockRandomNumberProvider.betweenZeroAndSpecified(2)).thenReturn(1);
        when(mockRandomNumberProvider.betweenZeroAndSpecified(4)).thenReturn(0);


    }

}
