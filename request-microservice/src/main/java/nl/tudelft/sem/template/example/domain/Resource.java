package nl.tudelft.sem.template.example.domain;

import lombok.EqualsAndHashCode;

/**
 * The type Resource.
 */
@EqualsAndHashCode
public class Resource {

    private String facultyName;
    private Double resourceCpu;
    private Double resourceGpu;
    private Double resourceMemory;

    /**
     * Instantiates a new Resource.
     *
     * @param facultyName    the faculty name
     * @param resourceCpu    the resource cpu
     * @param resourceGpu    the resource gpu
     * @param resourceMemory the resource memory
     */
    public Resource(String facultyName, Double resourceCpu, Double resourceGpu, Double resourceMemory) {
        this.facultyName = facultyName;
        this.resourceCpu = resourceCpu;
        this.resourceGpu = resourceGpu;
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
    public Double getResourceCpu() {
        return resourceCpu;
    }

    /**
     * Sets resource cpu.
     *
     * @param resourceCpu the resource cpu
     */
    public void setResourceCpu(Double resourceCpu) {
        this.resourceCpu = resourceCpu;
    }

    /**
     * Gets resource gpu.
     *
     * @return the resource gpu
     */
    public Double getResourceGpu() {
        return resourceGpu;
    }

    /**
     * Sets resource gpu.
     *
     * @param resourceGpu the resource gpu
     */
    public void setResourceGpu(Double resourceGpu) {
        this.resourceGpu = resourceGpu;
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
