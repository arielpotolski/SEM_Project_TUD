package nl.tudelft.sem.template.cluster.domain.strategies;

import nl.tudelft.sem.template.cluster.domain.cluster.FacultyTotalResources;

import java.util.List;

public interface NodeAssignmentStrategy {

    String pickFacultyToAssignNodeTo(List<FacultyTotalResources> faculties);

}
