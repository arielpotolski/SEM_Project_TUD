package nl.tudelft.sem.template.authentication.domain.exceptions;

import java.util.function.Supplier;

public class IdNotFoundException extends Exception {
    static final long serialVersionUID = -3387516993124229946L;

    public IdNotFoundException(long id) {
        super(String.valueOf(id));
    }
}
