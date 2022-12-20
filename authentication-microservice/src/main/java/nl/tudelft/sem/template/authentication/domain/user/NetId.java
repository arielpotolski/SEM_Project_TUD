package nl.tudelft.sem.template.authentication.domain.user;

import lombok.EqualsAndHashCode;


/**
 * A DDD value object representing a NetID in our domain.
 */
@EqualsAndHashCode
public class NetId {
    private final transient String netIdValue;

    public NetId(String netId) {
        this.netIdValue = netId;
    }

    @Override
    public String toString() {
        return netIdValue;
    }
}
