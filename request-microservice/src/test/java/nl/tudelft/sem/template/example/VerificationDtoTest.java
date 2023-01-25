package nl.tudelft.sem.template.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import nl.tudelft.sem.template.example.domain.VerificationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VerificationDtoTest {

    private VerificationDto dto;

    @BeforeEach
    public void setup() {
        this.dto = new VerificationDto("Vladi", "bearer", "EWI");
    }

    @Test
    public void gettersTest() {
        assertEquals(this.dto.getNetId(), "Vladi");
        assertEquals(this.dto.getToken(), "bearer");
        assertEquals(this.dto.getFaculty(), "EWI");
    }

}
