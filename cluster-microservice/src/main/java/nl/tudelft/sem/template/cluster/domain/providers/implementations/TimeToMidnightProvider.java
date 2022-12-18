package nl.tudelft.sem.template.cluster.domain.providers.implementations;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import nl.tudelft.sem.template.cluster.domain.providers.TimeProvider;

public class TimeToMidnightProvider implements TimeProvider {

    /**
     * Calculates the difference in milliseconds between the current time and midnight.
     *
     * @return a long representing this difference in milliseconds.
     */
    public long timeToMidnight() {
        LocalTime midnight = LocalTime.MIDNIGHT;
        LocalTime current = LocalTime.now();

        return 24 - ChronoUnit.MILLIS.between(current, midnight);
    }
}
