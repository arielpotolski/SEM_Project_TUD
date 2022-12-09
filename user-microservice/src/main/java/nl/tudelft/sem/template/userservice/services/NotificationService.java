package nl.tudelft.sem.template.userservice.services;

import nl.tudelft.sem.template.userservice.communicationData.JobNotificationData;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service class to store, handle, and sent out incoming notifications
 */
@Service
public class NotificationService {

    private final Map<String, List<JobNotificationData>> jobNotifications;

    /**
     * Constructor which initializes the data storage
     */
    public NotificationService() {
        this.jobNotifications = new HashMap<>();
    }

    public void addJobNotification(String netId, JobNotificationData jobNotificationData){
        if (!jobNotifications.containsKey(netId)) {
            jobNotifications.put(netId, List.of(jobNotificationData));
        }else{
            List<JobNotificationData> l = jobNotifications.get(netId);
            l.add(jobNotificationData);
            jobNotifications.put(netId, l);
        }
    }

    /**
     * Method which will return a list containing all notifications of the user, or an empty list if there are none
     * @param netId netId of the user
     * @return list containing notifications, can be emtpy
     */
    public List<JobNotificationData> getJobNotifications(String netId){
        if (jobNotifications.containsKey(netId)){
            List<JobNotificationData> l =  jobNotifications.get(netId);
            jobNotifications.remove(netId);
            return l;
        } else{
            return new ArrayList<>();
        }
    }

}
