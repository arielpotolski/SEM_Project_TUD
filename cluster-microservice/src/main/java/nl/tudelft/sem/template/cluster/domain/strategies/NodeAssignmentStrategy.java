package nl.tudelft.sem.template.cluster.domain.strategies;

import java.util.List;
import nl.tudelft.sem.template.cluster.models.FacultyResourcesResponseModel;

public interface NodeAssignmentStrategy {

    String pickFacultyToAssignNodeTo(List<FacultyResourcesResponseModel> faculties);

}
