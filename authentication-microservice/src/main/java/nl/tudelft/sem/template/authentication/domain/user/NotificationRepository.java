package nl.tudelft.sem.template.authentication.domain.user;

import java.util.List;
import nl.tudelft.sem.template.authentication.communicationdata.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Repository for storing Notifications by netID.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Looks for all notifications that are stored per netId.
     *
     * @param netId the netID to find the notifications by.
     *
     * @return a list of all notifications with a matching netID in the repository.
     */
    List<Notification> findByNetId(String netId);
}
