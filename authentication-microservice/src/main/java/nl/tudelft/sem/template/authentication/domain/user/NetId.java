package nl.tudelft.sem.template.authentication.domain.user;

import lombok.EqualsAndHashCode;


/**
 * A DDD value object representing a NetID in our domain.
 */
@EqualsAndHashCode
public class NetId {
    private final transient String netIdValue;

    /**
     * Creates NetId that is minimal 6 chars and max 20.
     *
     * @param netId String that is the netId
     */
    public NetId(String netId) {

        if (netId.length() > 20 || netId.length() < 6) {
            throw new IllegalArgumentException("Length of netId is incorrect");
        }
        this.netIdValue = netId;
    }

    @Override
    public String toString() {
        return netIdValue;
    }
}
