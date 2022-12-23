package nl.tudelft.sem.template.example.domain;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;

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

    public LocalDateTime getTimeLdt() {
        return LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }

    public LocalDate getTimeLd() {
        return LocalDate.ofInstant(clock.instant(), ZoneOffset.UTC);
    }


}
