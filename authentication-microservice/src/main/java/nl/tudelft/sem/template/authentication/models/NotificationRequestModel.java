package nl.tudelft.sem.template.authentication.models;

import lombok.Data;

@Data
public class NotificationRequestModel
{
    private String date;
    private String type;
    private String state;
    private String message;
    private String netId;
}
