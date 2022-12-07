package nl.tudelft.sem.template.example.domain;

import java.util.Date;

public class Request {

    private String name;
    private String description;
    private String faculty;

    private Double cpu;
    private Double gpu;
    private Double memory;

    private Date preferredDate;


    public Request(String name, String description,
                   String faculty, Double cpu, Double gpu, Double memory, Date preferredDate) {
        this.name = name;
        this.description = description;
        this.faculty = faculty;
        this.cpu = cpu;
        this.gpu = gpu;
        this.memory = memory;
        this.preferredDate = preferredDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    public Double getCpu() {
        return cpu;
    }

    public void setCpu(Double cpu) {
        this.cpu = cpu;
    }

    public Double getGpu() {
        return gpu;
    }

    public void setGpu(Double gpu) {
        this.gpu = gpu;
    }

    public Double getMemory() {
        return memory;
    }

    public void setMemory(Double memory) {
        this.memory = memory;
    }

    public Date getPreferredDate() {
        return preferredDate;
    }

    public void setPreferredDate(Date preferredDate) {
        this.preferredDate = preferredDate;
    }
}
