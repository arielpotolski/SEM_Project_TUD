package nl.tudelft.sem.template.cluster.domain.providers;

import java.time.LocalDate;

public interface DateProvider {

    LocalDate getCurrentDate();
    LocalDate getTomorrow();

}
