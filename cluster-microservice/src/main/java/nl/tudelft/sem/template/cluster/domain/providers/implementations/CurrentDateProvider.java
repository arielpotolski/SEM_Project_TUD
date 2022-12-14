package nl.tudelft.sem.template.cluster.domain.providers.implementations;

import java.time.LocalDate;
import nl.tudelft.sem.template.cluster.domain.providers.DateProvider;
import org.springframework.stereotype.Component;

@Component
public class CurrentDateProvider implements DateProvider {

    public LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    public LocalDate getTomorrow() {
        return LocalDate.now().plusDays(1);
    }

}
