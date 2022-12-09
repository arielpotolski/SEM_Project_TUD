package nl.tudelft.sem.template.userservice.serviceauthentication.models;

import lombok.Data;

/**
 * Model representing a registration request.
 */
@Data
public class RegistrationRequestModel {
    private String netId;
    private String password;
}