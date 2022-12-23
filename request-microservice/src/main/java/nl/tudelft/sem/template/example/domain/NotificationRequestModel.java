package nl.tudelft.sem.template.example.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestModel {
    private String date;
    private String type;
    private String state;
    private String message;
    private String netId;
}
