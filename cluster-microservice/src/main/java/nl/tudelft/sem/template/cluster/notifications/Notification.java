package nl.tudelft.sem.template.cluster.notifications;

import nl.tudelft.sem.template.cluster.domain.cluster.Job;

public class Notification {

    private String date;

    private String type;

    private String state;

    private String message;

    private String netId;

    /**
     * Initializes a new Notification.
     *
     * @param job the job of which the notification is about
     * @param netId the netId of the user the notification needs to be sent to.
     * @param state the state of the job.
     * @param date the date of the notification.
     */
    public Notification(Job job, String netId, String state, String date) {
        this.state = state;
        this.netId = netId;
        this.type = "job";
        this.date = date;
        this.message = job.toString();
    }

    /**
     * Sets the final sentence of the message of the notification according
     * to the state of it.
     *
     * @throws IllegalArgumentException if the notification has a state that
     *      does not require sending a notification to user.
     */
    public void setMessage() throws IllegalArgumentException {
        switch (this.state) {
            case "scheduled":
                this.message += "The job has been scheduled.";
                break;
            case "started":
                this.message += "The job has started.";
                break;
            case "completed":
                this.message += "The job has been completed";
                break;
            default:
                throw new IllegalArgumentException("Could not recognize the state of notification");
        }
    }

}
