package nl.tudelft.sem.template.example.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import nl.tudelft.sem.template.example.domain.Request;
import nl.tudelft.sem.template.example.domain.RequestRepository;
import nl.tudelft.sem.template.example.services.RequestAllocationService;

import java.util.List;

/**
 * The type Utils.
 */
public class Utils {


    /**
     * Checks if a given id is contained within a given array.
     *
     * @param ids the ids
     * @param id  the id
     * @return the boolean
     */
    public static boolean idIsContained(Long[] ids, long id) {
        for (Long along : ids) {
            if (along == id) {
                return true;
            }
        }
        return false;
    }


}
