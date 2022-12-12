package nl.tudelft.sem.template.example.domain;

import java.util.List;

public class AvailableResources {

    private List<Resource> resourceList;

    public AvailableResources(List<Resource> resourceList) {
        this.resourceList = resourceList;
    }

    public List<Resource> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<Resource> resourceList) {
        this.resourceList = resourceList;
    }
}
