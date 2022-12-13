package nl.tudelft.sem.template.cluster.domain.strategies;

import java.util.List;
import java.util.Random;
import nl.tudelft.sem.template.cluster.domain.cluster.FacultyTotalResources;

public class AssignNodeToRandomFacultyStrategy implements NodeAssignmentStrategy {

    public String pickFacultyToAssignNodeTo(List<FacultyTotalResources> faculties) {
        var rand = new Random();
        return faculties.get(rand.nextInt(faculties.size())).getFaculty_Id();
    }

}
