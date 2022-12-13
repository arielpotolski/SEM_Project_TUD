package nl.tudelft.sem.template.example.domain;

import java.util.List;

/**
 * The type Available resources.
 */
public class AvailableResources {

    private List<Resource> resourceList;

    /**
     * Instantiates a new Available resources.
     *
     * @param resourceList the resource list
     */
    public AvailableResources(List<Resource> resourceList) {
        this.resourceList = resourceList;
    }

    /**
     * Gets resource list.
     *
     * @return the resource list
     */
    public List<Resource> getResourceList() {
        return resourceList;
    }

    /**
     * Sets resource list.
     *
     * @param resourceList the resource list
     */
    public void setResourceList(List<Resource> resourceList) {
        this.resourceList = resourceList;
    }
}
