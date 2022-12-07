package nl.tudelft.sem.template.example.domain;

public class Resource {

    private String facultyName;
    private Double resourceCPU;
    private Double resourceGPU;
    private Double resourceMemory;

    public Resource(String facultyName, Double resourceCPU, Double resourceGPU, Double resourceMemory) {
        this.facultyName = facultyName;
        this.resourceCPU = resourceCPU;
        this.resourceGPU = resourceGPU;
        this.resourceMemory = resourceMemory;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public Double getResourceCPU() {
        return resourceCPU;
    }

    public void setResourceCPU(Double resourceCPU) {
        this.resourceCPU = resourceCPU;
    }

    public Double getResourceGPU() {
        return resourceGPU;
    }

    public void setResourceGPU(Double resourceGPU) {
        this.resourceGPU = resourceGPU;
    }

    public Double getResourceMemory() {
        return resourceMemory;
    }

    public void setResourceMemory(Double resourceMemory) {
        this.resourceMemory = resourceMemory;
    }
}
