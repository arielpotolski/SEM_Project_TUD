package nl.tudelft.sem.template.authentication.services;

import java.util.List;
import nl.tudelft.sem.template.authentication.domain.exceptions.NetIdAlreadyInUseException;
import nl.tudelft.sem.template.authentication.domain.exceptions.NetIdNotFoundException;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.NetId;
import nl.tudelft.sem.template.authentication.domain.user.Password;
import nl.tudelft.sem.template.authentication.domain.user.Role;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import org.springframework.stereotype.Service;

/**
 * A DDD service for registering a new user.
 */
@Service
public class RegistrationService {
    private final transient UserRepository userRepository;
    private final transient PasswordHashingService passwordHashingService;
    private final transient RoleControlService roleControlService;

    /**
     * Instantiates a new UserService.
     *
     * @param userRepository  the user repository
     * @param passwordHashingService the password encoder
     */
    public RegistrationService(UserRepository userRepository, PasswordHashingService passwordHashingService,
                               RoleControlService roleControlService) {
        this.userRepository = userRepository;
        this.passwordHashingService = passwordHashingService;
        this.roleControlService = roleControlService;
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

            // give role based on netId - SYSTEM is only used by services themselves
            String roleName;
            if (netId.toString().startsWith("adm")) {
                roleName = "SYSADMIN";
            } else if (netId.toString().startsWith("fac")) {
                roleName = "FACULTY";
            } else {
                roleName = "USER";
            }

            Role role = roleControlService.findByName(roleName);

            // set role
            user.setRole(role);

            userRepository.save(user);

            return user;
        }

        throw new NetIdAlreadyInUseException(netId);
    }

    /**
     * Changes the password of user.
     *
     * @param netId of the user
     * @param password new password
     * @throws Exception if the user does not exist
     */
    public void changePassword(NetId netId, Password password)throws  Exception {
        if (userRepository.existsByNetId(netId)) {
            HashedPassword hashedPassword = passwordHashingService.hash(password);
            AppUser user = userRepository.findByNetId(netId).get();
            user.changePassword(hashedPassword);
            userRepository.delete(user);
            userRepository.save(user);
            return;
        }
        throw new NetIdNotFoundException(netId);
    }

    /**
     * Add a faculty to a user.
     *
     * @param netId users you want to faculty to
     * @param faculty the faculty you want to add
     * @throws Exception if the user does not exist
     */
    public void applyFacultyUser(NetId netId, AppUser.Faculty faculty) throws Exception {
        if (!checkNetIdIsUnique(netId)) {
            AppUser user = userRepository.findByNetId(netId).get();
            user.addFaculty(faculty);
            userRepository.delete(user);
            userRepository.save(user);
            return;
        }
        //new make new exception
        throw new NetIdNotFoundException(netId);
    }


    /**
     * Get all the faculties of a user.
     *
     * @param netId user where the faculties are gotten from
     * @return List of all faculties of the user
     * @throws Exception if the netId does not exist
     */

    public List<AppUser.Faculty> getFaculties(NetId netId) throws Exception {
        if (!checkNetIdIsUnique(netId)) {
            AppUser user = userRepository.findByNetId(netId).get();
            return (List<AppUser.Faculty>) user.getFaculties();
        }
        //Make new exception
        throw new NetIdNotFoundException(netId);
    }

    /**
     * Remove faculty from a user.
     *
     * @param netId user where the faculty needs to be removed
     * @param faculty that needs to be removed
     * @throws NetIdNotFoundException if the netId does not exist
     */

    public void removeFacultyUser(NetId netId, AppUser.Faculty faculty) throws NetIdNotFoundException {
        if (!checkNetIdIsUnique(netId)) {
            AppUser user = userRepository.findByNetId(netId).get();
            user.removeFaculty(faculty);
            userRepository.delete(user);
            userRepository.save(user);
            return;
        }
        //new make new exception
        throw new NetIdNotFoundException(netId);
    }


    public boolean checkNetIdIsUnique(NetId netId) {
        return !userRepository.existsByNetId(netId);
    }

    public Role getRoleFromUser(NetId netId) {
        return userRepository.findByNetId(netId).orElseThrow().getRole();
    }
}
