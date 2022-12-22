package nl.tudelft.sem.template.authentication.domain.user;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@SpringBootTest
public class RoleTest {
    @Test
    public void equalsRolesTest() {
        Role role1 = new Role("test1");
        Role role2 = new Role("test1");
        Role role3 = new Role("test2");
        Role role4 = null;
        assertThat(role1.equals(role1)).isTrue();
        assertThat(role1.equals(role2)).isTrue();
        assertThat(role1.equals(role3)).isFalse();
        assertThat(role1.equals(role4)).isFalse();
        assertThat(role1.equals(new NetId("testUser1"))).isFalse();
    }
}
