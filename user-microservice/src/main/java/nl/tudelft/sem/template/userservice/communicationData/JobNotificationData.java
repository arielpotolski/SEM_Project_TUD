package nl.tudelft.sem.template.userservice.communicationData;

import java.util.Optional;

public class JobNotificationData {


    private State state;

}


enum State{
    SCHEDULED,
    STARTED,
    COMPLETED,
    DELAYED,
    DROPPED
}
