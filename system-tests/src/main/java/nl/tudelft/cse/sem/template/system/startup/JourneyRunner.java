package nl.tudelft.cse.sem.template.system.startup;

import nl.tudelft.cse.sem.template.system.domain.users.Paul;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class JourneyRunner implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        beginJourney();
    }

    private void beginJourney() {
        System.out.println("The journey begins with Paul...");
        Paul.embarkOnUserJourney();
    }

}
