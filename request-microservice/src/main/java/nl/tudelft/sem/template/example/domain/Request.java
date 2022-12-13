package nl.tudelft.sem.template.example.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.NonNull;


/**
 * The type Request.
 */
@Entity
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


    @JsonFormat(pattern = "yyyy-MM-dd")
    @NonNull
    private Date preferredDate;

    /**
     * Instantiates a new Request.
     *
     * @param id            the id
     * @param netId         the net id
     * @param name          the name
     * @param description   the description
     * @param faculty       the faculty
     * @param cpu           the cpu
     * @param gpu           the gpu
     * @param memory        the memory
     * @param approved      the approved
     * @param preferredDate the preferred date
     */
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

    /**
     * Instantiates a new Request.
     */
    public Request() {

    }

    /**
     * Gets net id.
     *
     * @return the net id
     */
    public String getNetId() {
        return netId;
    }

    /**
     * Sets net id.
     *
     * @param netId the net id
     */
    public void setNetId(String netId) {
        this.netId = netId;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets faculty.
     *
     * @return the faculty
     */
    public String getFaculty() {
        return faculty;
    }

    /**
     * Sets faculty.
     *
     * @param faculty the faculty
     */
    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }

    /**
     * Gets cpu.
     *
     * @return the cpu
     */
    public Double getCpu() {
        return cpu;
    }

    /**
     * Sets cpu.
     *
     * @param cpu the cpu
     */
    public void setCpu(Double cpu) {
        this.cpu = cpu;
    }

    /**
     * Gets gpu.
     *
     * @return the gpu
     */
    public Double getGpu() {
        return gpu;
    }

    /**
     * Sets gpu.
     *
     * @param gpu the gpu
     */
    public void setGpu(Double gpu) {
        this.gpu = gpu;
    }

    /**
     * Gets memory.
     *
     * @return the memory
     */
    public Double getMemory() {
        return memory;
    }

    /**
     * Sets memory.
     *
     * @param memory the memory
     */
    public void setMemory(Double memory) {
        this.memory = memory;
    }

    /**
     * Gets preferred date.
     *
     * @return the preferred date
     */
    public Date getPreferredDate() {
        return preferredDate;
    }

    /**
     * Sets preferred date.
     *
     * @param preferredDate the preferred date
     */
    public void setPreferredDate(Date preferredDate) {
        this.preferredDate = preferredDate;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Is approved boolean.
     *
     * @return the boolean
     */
    public boolean isApproved() {
        return approved;
    }

    /**
     * Sets approved.
     *
     * @param approved the approved
     */
    public void setApproved(boolean approved) {
        this.approved = approved;
    }
}
