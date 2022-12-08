package nl.tudelft.sem.template.example.domain;

public class VerificationDTO {

    private String netId;
    private String token;
    private String faculty;

    public VerificationDTO(String netId, String token, String faculty) {
        this.netId = netId;
        this.token = token;
        this.faculty = faculty;
    }


    public String getNetId() {
        return netId;
    }

    public void setNetId(String netId) {
        this.netId = netId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getFaculty() {
        return faculty;
    }

    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }
}
