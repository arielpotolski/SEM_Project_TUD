package nl.tudelft.sem.template.example.domain;

/**
 * The type Verification dto.
 */
public class VerificationDTO {

    private String netId;
    private String token;
    private String faculty;

    /**
     * Instantiates a new Verification dto.
     *
     * @param netId   the net id
     * @param token   the token
     * @param faculty the faculty
     */
    public VerificationDTO(String netId, String token, String faculty) {
        this.netId = netId;
        this.token = token;
        this.faculty = faculty;
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
     * Gets token.
     *
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * Sets token.
     *
     * @param token the token
     */
    public void setToken(String token) {
        this.token = token;
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
}
