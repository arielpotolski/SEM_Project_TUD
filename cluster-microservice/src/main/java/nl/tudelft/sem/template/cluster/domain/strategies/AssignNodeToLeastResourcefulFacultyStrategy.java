package nl.tudelft.sem.template.cluster.domain.strategies;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import nl.tudelft.sem.template.cluster.domain.cluster.FacultyTotalResources;

public class AssignNodeToLeastResourcefulFacultyStrategy implements NodeAssignmentStrategy {

    /**
     * A helper class to facilitate the usage of functional Java.
     *
     * @param <K> the key of the Pair.
     * @param <V> the value of the Pair.
     */
    private static class Pair<K, V> {
        K key;
        V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }
    }

    /**
     * Finds the faculty with the least total currently assigned resources and returns its facultyId.
     *
     * @param faculties the faculties from which to look for the least resourceful one.
     *
     * @return String representation of the facultyId.
     */
    public String pickFacultyToAssignNodeTo(List<FacultyTotalResources> faculties) {
        return faculties.stream()
                .map(x -> new Pair<String, Double>(x.getFaculty_Id(),
                        x.getCpu_Resources() + x.getGpu_Resources() + x.getMemory_Resources()))
                .sorted(Comparator.comparingDouble(x -> x.value)).map(x -> x.key).collect(Collectors.toList()).get(0);
    }

}
