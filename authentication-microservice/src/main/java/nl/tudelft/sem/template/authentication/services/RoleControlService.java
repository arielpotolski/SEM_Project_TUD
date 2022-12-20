package nl.tudelft.sem.template.authentication.services;

import nl.tudelft.sem.template.authentication.domain.user.Role;
import nl.tudelft.sem.template.authentication.domain.user.RoleRepository;
import org.springframework.stereotype.Service;

@Service
public class RoleControlService {

    private final transient RoleRepository roleRepository;

    public RoleControlService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public long count() {
        return this.roleRepository.count();
    }

    public void save(Role role) {
        this.roleRepository.save(role);
    }

    public Role findByName(String name) {
        return this.roleRepository.findRoleByName(name).get();
    }

}
