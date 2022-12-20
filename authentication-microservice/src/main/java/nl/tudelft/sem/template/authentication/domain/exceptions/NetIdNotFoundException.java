package nl.tudelft.sem.template.authentication.domain.exceptions;

import nl.tudelft.sem.template.authentication.domain.user.NetId;

public class NetIdNotFoundException extends Exception {
    static final long serialVersionUID = -3387516993124229947L;

    public NetIdNotFoundException(NetId netId) {
        super(netId.toString() + " is not found");
    }
}
