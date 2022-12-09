package nl.tudelft.sem.template.userservice.communicationUtil;

/**
 * Class for receiving Job Notification Data
 */
public class JobNotificationState {


    private State state;
    public JobNotificationState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    enum State{
        a, b
    }
}
