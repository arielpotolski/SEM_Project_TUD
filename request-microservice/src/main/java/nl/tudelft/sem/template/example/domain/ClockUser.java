package nl.tudelft.sem.template.example.domain;


import org.springframework.stereotype.Component;

import java.time.*;

@Component
public class ClockUser implements DateProvider {

    private Clock clock;

    public ClockUser(Clock clock) {
        this.clock = clock;
    }

    public ClockUser() {
        this.clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"));
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
