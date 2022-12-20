package nl.tudelft.sem.template.authentication.domain.exceptions;

import nl.tudelft.sem.template.authentication.domain.user.NetId;

/**
 * Exception to indicate the NetID is already in use.
 */
public class NetIdAlreadyInUseException extends Exception {
    static final long serialVersionUID = -3387516993124229948L;
    
    public NetIdAlreadyInUseException(NetId netId) {
        super(netId.toString());
    }
}
