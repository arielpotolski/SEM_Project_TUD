package nl.tudelft.sem.template.authentication.services;

import nl.tudelft.sem.template.authentication.domain.user.Role;
import nl.tudelft.sem.template.authentication.domain.user.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleControlService {

    private final transient RoleRepository roleRepository;

    @Autowired
    public RoleControlService (RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Populates the role repository. No new roles are expected so this should only be done once and with no parameters.
     */
    public void initialize() {
        this.roleRepository.save(new Role("USER"));
        this.roleRepository.save(new Role("FACULTY"));
        this.roleRepository.save(new Role("SYSADMIN"));
        this.roleRepository.save(new Role("SYSTEM"));
    }

    public Role findByName(String name) {
        return this.roleRepository.findRoleByName(name).get();
    }

}
