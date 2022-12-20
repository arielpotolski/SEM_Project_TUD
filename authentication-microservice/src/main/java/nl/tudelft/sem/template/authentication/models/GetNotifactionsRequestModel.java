package nl.tudelft.sem.template.authentication.models;

import lombok.Data;

@Data
public class GetNotifactionsRequestModel {
    //String netId;
    String dateUntil;
    String dateFrom;

    public void check() {
        return;
    }
}
