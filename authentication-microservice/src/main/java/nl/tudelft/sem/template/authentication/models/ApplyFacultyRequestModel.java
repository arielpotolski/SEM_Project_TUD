package nl.tudelft.sem.template.authentication.models;

import lombok.Data;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;

@Data
public class ApplyFacultyRequestModel {
    private String netId;
    private String faculty;
}
