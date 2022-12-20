package nl.tudelft.sem.template.authentication.authentication;

import java.util.Set;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.NetId;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * User details service responsible for retrieving the user from the DB.
 */
@Service
public class JwtUserDetailsService implements UserDetailsService {

    private final transient UserRepository userRepository;

    @Autowired
    public JwtUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads user information required for authentication from the DB.
     *
     * @param username The username of the user we want to authenticate
     * @return The authentication user information of that user
     * @throws UsernameNotFoundException Username was not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var optionalUser = userRepository.findByNetId(new NetId(username));

        if (optionalUser.isEmpty()) {
            throw new UsernameNotFoundException("User does not exist");
        }

        var user = optionalUser.get();

        return new User(user.getNetId().toString(), user.getPassword().toString(),
                getAuthority(user));
    }

    private Set<SimpleGrantedAuthority> getAuthority(AppUser user) {
        return Set.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));
    }
}
