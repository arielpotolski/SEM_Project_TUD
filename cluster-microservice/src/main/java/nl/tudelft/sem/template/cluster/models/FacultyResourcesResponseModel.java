package nl.tudelft.sem.template.cluster.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.cluster.domain.cluster.FacultyTotalResources;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacultyResourcesResponseModel {
    private String facultyId;
    private double totalCpu;
    private double totalGpu;
    private double totalMemory;

    /**
     * Converts the provided FacultyTotalResources SPI into a response model.
     *
     * @param rawResources the SPI to convert.
     *
     * @return the response model based on the provided FacultyTotalResources object.
     */
    private static FacultyResourcesResponseModel convertFacultyTotalResourcesToResponseModel(
            FacultyTotalResources rawResources) {
        return new FacultyResourcesResponseModel(rawResources.getFaculty_Id(), rawResources.getCpu_Resources(),
                rawResources.getGpu_Resources(), rawResources.getMemory_Resources());
    }

    /**
     * Converts all Spring Projection Interfaces of FacultyTotalResources class into FacultyResourcesResponseModels.
     *
     * @param rawResources the list of interfaces to be converted into response models.
     *
     * @return a list of response models based on the provided input.
     */
    public static List<FacultyResourcesResponseModel> convertAllFacultyTotalResourcesToResponseModels(
            List<FacultyTotalResources> rawResources) {
        List<FacultyResourcesResponseModel> models = new ArrayList<>();
        for (FacultyTotalResources resources : rawResources) {
            models.add(FacultyResourcesResponseModel.convertFacultyTotalResourcesToResponseModel(resources));
        }
        return models;
    }
}
