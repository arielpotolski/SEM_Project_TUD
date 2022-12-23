package nl.tudelft.cse.sem.template.system.domain.users;

public class Xavier implements JourneyingUser {

    public Xavier() {}

    private static void setup() {

    }

    public static void embarkOnUserJourney() {
        System.out.println("Xavier begins by submitting a multitude of requests...");

        // try to submit request but fail

        // authenticate, try again

        // try to see the available resources

        // send many requests, exhausting resources, wrong faculties

        // check notifications in user

        // try to send job directly to cluster

        System.exit(0);
    }

}
