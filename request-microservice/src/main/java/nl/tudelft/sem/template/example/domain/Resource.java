package nl.tudelft.sem.template.example.domain;

/**
 * The type Resource.
 */
public class Resource {

    private String facultyName;
    private Double resourceCPU;
    private Double resourceGPU;
    private Double resourceMemory;

    /**
     * Instantiates a new Resource.
     *
     * @param facultyName    the faculty name
     * @param resourceCPU    the resource cpu
     * @param resourceGPU    the resource gpu
     * @param resourceMemory the resource memory
     */
    public Resource(String facultyName, Double resourceCPU, Double resourceGPU, Double resourceMemory) {
        this.facultyName = facultyName;
        this.resourceCPU = resourceCPU;
        this.resourceGPU = resourceGPU;
        this.resourceMemory = resourceMemory;
    }

    /**
     * Gets faculty name.
     *
     * @return the faculty name
     */
    public String getFacultyName() {
        return facultyName;
    }

    /**
     * Sets faculty name.
     *
     * @param facultyName the faculty name
     */
    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    /**
     * Gets resource cpu.
     *
     * @return the resource cpu
     */
    public Double getResourceCPU() {
        return resourceCPU;
    }

    /**
     * Sets resource cpu.
     *
     * @param resourceCPU the resource cpu
     */
    public void setResourceCPU(Double resourceCPU) {
        this.resourceCPU = resourceCPU;
    }

    /**
     * Gets resource gpu.
     *
     * @return the resource gpu
     */
    public Double getResourceGPU() {
        return resourceGPU;
    }

    /**
     * Sets resource gpu.
     *
     * @param resourceGPU the resource gpu
     */
    public void setResourceGPU(Double resourceGPU) {
        this.resourceGPU = resourceGPU;
    }

    /**
     * Gets resource memory.
     *
     * @return the resource memory
     */
    public Double getResourceMemory() {
        return resourceMemory;
    }

    /**
     * Sets resource memory.
     *
     * @param resourceMemory the resource memory
     */
    public void setResourceMemory(Double resourceMemory) {
        this.resourceMemory = resourceMemory;
    }
}
