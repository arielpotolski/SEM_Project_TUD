package nl.tudelft.sem.template.example.util;

public class Utils {


    public static boolean idIsContained(Long[] ids,long id){
        for (Long aLong : ids) {
            if (aLong == id) {
                return true;
            }
        }
        return false;
    }


}
