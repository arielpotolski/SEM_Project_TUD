package nl.tudelft.sem.template.cluster.profiles;

import nl.tudelft.sem.template.cluster.domain.providers.implementations.RandomNumberProvider;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("mockRandomNumberProvider")
@Configuration
public class MockRandomNumberProviderProfile {

    @Bean
    @Primary  // marks this bean as the first bean to use when trying to inject an AuthenticationManager
    public RandomNumberProvider getMockRandomNumberProvider() {
        return Mockito.mock(RandomNumberProvider.class);
    }

}
