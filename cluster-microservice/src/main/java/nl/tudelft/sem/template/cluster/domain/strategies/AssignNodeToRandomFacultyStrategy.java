package nl.tudelft.sem.template.cluster.domain.strategies;

import java.util.List;
import nl.tudelft.sem.template.cluster.domain.providers.NumberProvider;
import nl.tudelft.sem.template.cluster.models.FacultyResourcesResponseModel;
import org.springframework.stereotype.Component;

@Component
public class AssignNodeToRandomFacultyStrategy implements NodeAssignmentStrategy {

    private final transient NumberProvider numberProvider;

    public AssignNodeToRandomFacultyStrategy(NumberProvider numberProvider) {
        this.numberProvider = numberProvider;
    }

    public String pickFacultyToAssignNodeTo(List<FacultyResourcesResponseModel> faculties) {
        var rand = numberProvider.betweenZeroAndSpecified(faculties.size());
        return faculties.get(rand).getFacultyName();
    }

}
