package nl.tudelft.sem.template.cluster.models;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.cluster.domain.cluster.FacultyDatedTotalResources;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FacultyDatedResourcesResponseModel {
    private LocalDate date;
    private String facultyId;
    private double totalCpu;
    private double totalGpu;
    private double totalMemory;

    /**
     * Converts the input from Spring Projection Interfaces from SQL queries into response models to be returned through
     * HTTP.
     *
     * @param rawData the data to convert.
     *
     * @return converted data as a list.
     */
    public static List<FacultyDatedResourcesResponseModel> convertToResponseModels(
            List<FacultyDatedTotalResources> rawData) {
        return rawData.stream().map(x -> new FacultyDatedResourcesResponseModel(x.getScheduled_Date(),
                        x.getFaculty_Id(), x.getCpu_Resources(),
                        x.getGpu_Resources(), x.getMemory_Resources()))
                .collect(Collectors.toList());
    }
}
