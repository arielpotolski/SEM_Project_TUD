package nl.tudelft.sem.template.example.domain;


import java.time.LocalDate;
import java.time.LocalDateTime;

public interface DateProvider {

    LocalDateTime getTimeLDT();
    LocalDate getTimeLD();


}
