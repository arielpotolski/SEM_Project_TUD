package nl.tudelft.sem.template.authentication.domain.user;

public class NetIdNotFoundException extends Exception {

    public NetIdNotFoundException(NetId netId) {
        super(netId.toString() + " is not found");
    }
}
