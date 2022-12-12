package nl.tudelft.sem.template.cluster.domain.strategies;

import java.util.List;
import nl.tudelft.sem.template.cluster.domain.cluster.FacultyTotalResources;

public interface NodeAssignmentStrategy {

    String pickFacultyToAssignNodeTo(List<FacultyTotalResources> faculties);

}
