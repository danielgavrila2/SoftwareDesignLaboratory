package com.sd.enterprise.mvcapp;

import com.sd.enterprise.mvcapp.dto.UserResponse;
import com.sd.enterprise.mvcapp.entity.Role;
import com.sd.enterprise.mvcapp.entity.User;
import com.sd.enterprise.mvcapp.exception.ResourceNotFoundException;
import com.sd.enterprise.mvcapp.repository.UserRepository;
import com.sd.enterprise.mvcapp.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        adminUser   = TestDataFactory.buildAdminUser();
        regularUser = TestDataFactory.buildRegularUser();
    }

    // ─── getAllUsers ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllUsers()")
    class GetAllUsers {

        @Test
        @DisplayName("returns mapped UserResponse list for all users")
        void returnsAllUsers() {
            when(userRepository.findAll()).thenReturn(List.of(adminUser, regularUser));

            List<UserResponse> result = userService.getAllUsers();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(UserResponse::getEmail)
                    .containsExactlyInAnyOrder("admin@test.com", "user@test.com");
        }

        @Test
        @DisplayName("returns empty list when no users exist")
        void returnsEmptyList() {
            when(userRepository.findAll()).thenReturn(List.of());

            assertThat(userService.getAllUsers()).isEmpty();
        }
    }

    // ─── getUserById ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getUserById()")
    class GetUserById {

        @Test
        @DisplayName("returns correct UserResponse for existing user")
        void returnsExistingUser() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

            UserResponse result = userService.getUserById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getEmail()).isEqualTo("admin@test.com");
            assertThat(result.getRole()).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException for unknown id")
        void throwsForUnknownId() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ─── getCurrentUser ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("getCurrentUser()")
    class GetCurrentUser {

        @Test
        @DisplayName("returns user matching the authenticated email")
        void returnsAuthenticatedUser() {
            mockSecurityContext(regularUser.getEmail());
            when(userRepository.findByEmail(regularUser.getEmail())).thenReturn(Optional.of(regularUser));

            UserResponse result = userService.getCurrentUser();

            assertThat(result.getEmail()).isEqualTo("user@test.com");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when authenticated user no longer exists")
        void throwsWhenUserDeleted() {
            mockSecurityContext("ghost@test.com");
            when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getCurrentUser())
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─── updateUserRole ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateUserRole()")
    class UpdateUserRole {

        @Test
        @DisplayName("promotes USER to ADMIN and persists change")
        void promotesToAdmin() {
            when(userRepository.findById(2L)).thenReturn(Optional.of(regularUser));
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UserResponse result = userService.updateUserRole(2L, Role.ADMIN);

            assertThat(result.getRole()).isEqualTo(Role.ADMIN);
            verify(userRepository).save(argThat(u -> u.getRole() == Role.ADMIN));
        }

        @Test
        @DisplayName("demotes ADMIN to USER")
        void demotesToUser() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            UserResponse result = userService.updateUserRole(1L, Role.USER);

            assertThat(result.getRole()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException for unknown user")
        void throwsForUnknownUser() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUserRole(99L, Role.ADMIN))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─── toggleUserEnabled ────────────────────────────────────────────────────

    @Nested
    @DisplayName("toggleUserEnabled()")
    class ToggleUserEnabled {

        @Test
        @DisplayName("disables an active user")
        void disablesActiveUser() {
            User activeUser = TestDataFactory.buildRegularUser(); // enabled=true
            when(userRepository.findById(2L)).thenReturn(Optional.of(activeUser));
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            userService.toggleUserEnabled(2L);

            assertThat(activeUser.isEnabled()).isFalse();
            verify(userRepository).save(activeUser);
        }

        @Test
        @DisplayName("re-enables a disabled user")
        void enablesDisabledUser() {
            User disabledUser = TestDataFactory.buildDisabledUser(); // enabled=false
            when(userRepository.findById(3L)).thenReturn(Optional.of(disabledUser));
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            userService.toggleUserEnabled(3L);

            assertThat(disabledUser.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("throws ResourceNotFoundException for unknown user")
        void throwsForUnknownUser() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.toggleUserEnabled(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ─── deleteUser ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteUser()")
    class DeleteUser {

        @Test
        @DisplayName("deletes user when they exist")
        void deletesExistingUser() {
            when(userRepository.existsById(2L)).thenReturn(true);

            userService.deleteUser(2L);

            verify(userRepository).deleteById(2L);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException and does not call deleteById")
        void throwsAndSkipsDeleteWhenNotFound() {
            when(userRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> userService.deleteUser(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(userRepository, never()).deleteById(any());
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void mockSecurityContext(String email) {
        Authentication auth = mock(Authentication.class);
        SecurityContext ctx  = mock(SecurityContext.class);
        when(auth.getName()).thenReturn(email);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }
}