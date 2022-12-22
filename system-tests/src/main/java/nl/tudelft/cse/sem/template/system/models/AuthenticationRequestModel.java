package nl.tudelft.cse.sem.template.system.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequestModel {
    private String netId;
    private String password;
}
