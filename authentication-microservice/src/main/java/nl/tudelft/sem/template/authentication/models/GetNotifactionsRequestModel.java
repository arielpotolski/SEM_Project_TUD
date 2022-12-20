package nl.tudelft.sem.template.authentication.models;

import lombok.Data;

@Data
public class GetNotifactionsRequestModel {
    //String netId;
    String start;
    String end;

    public void check() {
        return;
    }
}
