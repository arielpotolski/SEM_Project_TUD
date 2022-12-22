package nl.tudelft.sem.template.example.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResourceResponseModel {
    private String facultyName;
    private double resourceCpu;
    private double resourceGpu;
    private double resourceMemory;
}
