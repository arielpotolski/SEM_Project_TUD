package nl.tudelft.sem.template.authentication.domain.user;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * A DDD service for registering a new user.
 */
@Service
public class RegistrationService {
    private final transient UserRepository userRepository;
    private final transient PasswordHashingService passwordHashingService;

    /**
     * Instantiates a new UserService.
     *
     * @param userRepository  the user repository
     * @param passwordHashingService the password encoder
     */
    public RegistrationService(UserRepository userRepository, PasswordHashingService passwordHashingService) {
        this.userRepository = userRepository;
        this.passwordHashingService = passwordHashingService;
    }

    /**
     * Register a new user.
     *
     * @param netId    The NetID of the user
     * @param password The password of the user
     * @throws Exception if the user already exists
     */
    public AppUser registerUser(NetId netId, Password password) throws Exception {

        if (checkNetIdIsUnique(netId)) {
            // Hash password
            HashedPassword hashedPassword = passwordHashingService.hash(password);

            // Create new account
            AppUser user = new AppUser(netId, hashedPassword);

            userRepository.save(user);

            return user;
        }

        throw new NetIdAlreadyInUseException(netId);
    }

    public void changePassword(NetId netId, Password password)throws  Exception{
        if (!checkNetIdIsUnique(netId)) {
            HashedPassword hashedPassword = passwordHashingService.hash(password);
            AppUser user = userRepository.findByNetId(netId).get();
            System.out.println(user.getPassword());
            user.changePassword(hashedPassword);
            System.out.println(user.getPassword());
            userRepository.save(user);

            System.out.println(userRepository.count());
            return;
        }
        throw new NetIdAlreadyInUseException(netId);
    }
//    public void applyFacultyUser(NetId netId, AppUser.Faculty faculty) throws Exception {
//        if(!checkNetIdIsUnique(netId)){
//            Optional<AppUser> user = userRepository.findByNetId(netId);
//
//        }
//    }

    public boolean checkNetIdIsUnique(NetId netId) {
        return !userRepository.existsByNetId(netId);
    }
}
