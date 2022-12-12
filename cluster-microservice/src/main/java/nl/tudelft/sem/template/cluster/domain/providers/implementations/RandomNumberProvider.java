package nl.tudelft.sem.template.cluster.domain.providers.implementations;

import nl.tudelft.sem.template.cluster.domain.providers.NumberProvider;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class RandomNumberProvider implements NumberProvider {

    public int betweenZeroAndSpecified(int n) {
        return new Random().nextInt(n);
    }

}
