package nl.tudelft.sem.template.cluster.domain.strategies;

import java.util.List;
import nl.tudelft.sem.template.cluster.domain.cluster.FacultyTotalResources;
import nl.tudelft.sem.template.cluster.domain.providers.NumberProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AssignNodeToRandomFacultyStrategy implements NodeAssignmentStrategy {

    @Autowired
    private transient NumberProvider numberProvider;

    public String pickFacultyToAssignNodeTo(List<FacultyTotalResources> faculties) {
        var rand = numberProvider.betweenZeroAndSpecified(faculties.size());
        return faculties.get(rand).getFaculty_Id();
    }

}
