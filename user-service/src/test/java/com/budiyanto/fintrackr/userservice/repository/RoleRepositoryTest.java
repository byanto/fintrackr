package com.budiyanto.fintrackr.userservice.repository;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.budiyanto.fintrackr.userservice.TestcontainersConfiguration;
import com.budiyanto.fintrackr.userservice.domain.Role;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@DisplayName("RoleRepository Test")
class RoleRepositoryTest {
    
    private final RoleRepository roleRepository;
    
    @Autowired
    RoleRepositoryTest(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Test
    @DisplayName("should save and retrieve role")
    void should_saveAndRetrieveRole() {
        // Arrange: Create a new Role object
        String roleName = "ROLE_TEST";
        Role role = new Role(roleName);

        // Act: Save the role using the repository
        Role savedRole = roleRepository.save(role);

        // Assert: Verify that the role was saved correctly
        assertThat(savedRole).isNotNull();
        assertThat(savedRole.getId()).isGreaterThan(0);
        assertThat(savedRole.getName()).isEqualTo(roleName);

        // Act: Retrieve the saved role
        Role retrievedRole = roleRepository.findById(savedRole.getId()).orElse(null);

        // Assert: Verify that the role can be retrieved
        assertThat(retrievedRole).isNotNull();
        assertThat(retrievedRole.getId()).isEqualTo(savedRole.getId());
        assertThat(retrievedRole.getName()).isEqualTo(roleName);
    }

    @Test
    @DisplayName("should retrieve role when name exists")
    void should_retrieveRole_when_nameExists() {
        // Arrange: The 'ROLE_USER' role is pre-loaded by the V1 migration script.
        String roleName = "ROLE_USER";

        // Act
        Role foundRole = roleRepository.findByName(roleName).orElse(null);

        // Assert
        assertThat(foundRole).isNotNull();
        assertThat(foundRole.getName()).isEqualTo(roleName);
    }

    @Test
    @DisplayName("should return empty when retrieving non-existent name")
    void should_returnEmpty_when_retrievingNonExistentName() {
        // Arrange
        String nonExistentRoleName = "ROLE_GUEST";

        // Act & Assert
        Optional<Role> foundRole = roleRepository.findByName(nonExistentRoleName);
        assertThat(foundRole).isNotPresent();
    }
}
