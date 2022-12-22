package nl.tudelft.sem.template.example.domain;

import org.springframework.stereotype.Component;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class ClockUser {

    private Clock clock;

    public ClockUser(Clock clock) {
        this.clock = clock;
    }

    public ClockUser() {
    }

    public Clock getClock() {
        return clock;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public LocalDateTime getTimeLDT() {
        return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    public LocalDate getTimeLD() {
        return LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
    }
}
