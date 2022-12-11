package nl.tudelft.sem.template.cluster.domain.strategies;

import nl.tudelft.sem.template.cluster.domain.cluster.FacultyTotalResources;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AssignNodeToLeastResourcefulFacultyStrategy implements NodeAssignmentStrategy{

    private static class Pair<K, V> {
        K key;
        V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    public String pickFacultyToAssignNodeTo(List<FacultyTotalResources> faculties) {
        return faculties.stream()
                .map(x -> new Pair<String, Double>(x.getFaculty_Id(),
                        x.getCpu_Resources() + x.getGpu_Resources() + x.getMemory_Resources()))
                .sorted(Comparator.comparingDouble(x -> x.value)).map(x -> x.key).collect(Collectors.toList()).get(0);
    }

}
