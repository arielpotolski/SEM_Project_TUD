package nl.tudelft.sem.template.authentication.domain.user;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.authentication.domain.HasEvents;


/**
 * A DDD entity representing an application user in our domain.
 */
@Entity
@NoArgsConstructor
public class AppUser extends HasEvents {
    public enum Faculty {
        CIVIL,
        IO,
        EWI
    }
    /**
     * Identifier for the application user.
     */

    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Column(name = "net_id", nullable = false, unique = true)
    @Convert(converter = NetIdAttributeConverter.class)
    private NetId netId;

    @Column(name = "password_hash", nullable = false)
    @Convert(converter = HashedPasswordAttributeConverter.class)
    private HashedPassword password;

    @ElementCollection(targetClass = Faculty.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "faculties")
    @Column(name = "faculty")
    private Collection<Faculty> facultyList;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinTable(name = "USER_ROLES",
                joinColumns = {
                    @JoinColumn(name = "USER_ID", referencedColumnName = "id")
                },
                inverseJoinColumns = {
                    @JoinColumn(name = "ROLE_ID", referencedColumnName = "id") })
    private Role role;

    /**
     * Create new application user.
     *
     * @param netId The NetId for the new user
     * @param password The password for the new user
     */
    public AppUser(NetId netId, HashedPassword password) {
        this.netId = netId;
        this.password = password;
        this.recordThat(new UserWasCreatedEvent(netId));
        this.facultyList = Collections.emptyList();
    }

    public void changePassword(HashedPassword password) {
        this.password = password;
        this.recordThat(new PasswordWasChangedEvent(this));
    }

    public NetId getNetId() {
        return netId;
    }

    public HashedPassword getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Equality is only based on the identifier.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AppUser appUser = (AppUser) o;
        return id.equals(appUser.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(netId);
    }

    /**
     * Adds faculy to this user.
     *
     * @param faculty to add
     */

    public void addFaculty(Faculty faculty) {
        if (!this.facultyList.contains(faculty)) {
            this.facultyList.add(faculty);
        }
    }

    public Collection<Faculty> getFaculties() {
        return this.facultyList;
    }

    /**
     * removing faculty from the user.
     *
     * @param faculty that needs to be removed
     */
    public void removeFaculty(Faculty faculty) {
        if (this.facultyList.contains(faculty)) {
            this.facultyList.remove(faculty);
        }
    }
}
