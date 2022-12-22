package nl.tudelft.sem.template.cluster.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.cluster.domain.cluster.AvailableResourcesForDate;
import nl.tudelft.sem.template.cluster.domain.cluster.FacultyTotalResources;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacultyResourcesResponseModel {
    private String facultyName;
    private double resourceCpu;
    private double resourceGpu;
    private double resourceMemory;

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

    /**
     * Converts resources available to a faculty on given date to a faculty with assigned resources because this is
     * what request wants for some reason.
     *
     * @param rawResources the available resources to convert
     * @param facultyId the facultyId to whom the resources are assigned.
     *
     * @return a list of objects that the request service can hopefully read.
     */
    public static List<FacultyResourcesResponseModel> convertForRequestService(
            List<AvailableResourcesForDate> rawResources, String facultyId) {
        List<FacultyResourcesResponseModel> models = new ArrayList<>();
        for (AvailableResourcesForDate resources : rawResources) {
            var model = Stream.of(resources).map(x -> new FacultyResourcesResponseModel(
                    facultyId, x.getAvailableCpu(), x.getAvailableGpu(), x.getAvailableMemory()
            )).collect(Collectors.toList()).get(0);
            models.add(model);
        }
        return models;
    }
}
