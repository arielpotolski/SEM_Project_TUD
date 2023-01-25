package nl.tudelft.sem.template.example.util;



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
