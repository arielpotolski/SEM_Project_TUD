package nl.tudelft.sem.template.userservice.serviceauthentication.models;

import lombok.Data;

/**
 * Model representing an authentication request.
 */
@Data
public class AuthenticationRequestModel {
    private String netId;
    private String password;
}