package nl.tudelft.sem.template.example.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.NonNull;

import javax.persistence.*;
import java.util.Date;


@Entity
@Table(name = "request")
public class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NonNull
    private String netId;

    @NonNull
    private String name;
    @NonNull
    private String description;
    @NonNull
    private String faculty;

    @NonNull
    private Double cpu;
    @NonNull
    private Double gpu;
    @NonNull
    private Double memory;

    @NonNull
    private boolean approved;

    @NonNull
    @JsonFormat(pattern="yyyy-MM-dd")
    private Date preferredDate;

    public Request(Long id, String netId, String name, String description, String faculty,
                   Double cpu, Double gpu, Double memory,
                   boolean approved, Date preferredDate) {
        this.id = id;
        this.netId = netId;
        this.name = name;
        this.description = description;
        this.faculty = faculty;
        this.cpu = cpu;
        this.gpu = gpu;
        this.memory = memory;
        this.approved = approved;
        this.preferredDate = preferredDate;
    }

    public Request() {

    }

    public String getNetId() {
        return netId;
    }

    public void setNetId(String netId) {
        this.netId = netId;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }
}
