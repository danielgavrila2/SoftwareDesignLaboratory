package com.sd.enterprise.mvcapp;

import com.sd.enterprise.mvcapp.dto.AuthResponse;
import com.sd.enterprise.mvcapp.dto.LoginRequest;
import com.sd.enterprise.mvcapp.dto.RegisterRequest;
import com.sd.enterprise.mvcapp.entity.Role;
import com.sd.enterprise.mvcapp.entity.User;
import com.sd.enterprise.mvcapp.exception.EmailAlreadyExistsException;
import com.sd.enterprise.mvcapp.repository.UserRepository;
import com.sd.enterprise.mvcapp.security.JwtService;
import com.sd.enterprise.mvcapp.services.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @InjectMocks
    private AuthService authService;

    // ─── register ─────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("successfully registers a new user and returns JWT")
        void successfulRegistration() {
            RegisterRequest req = TestDataFactory.buildRegisterRequest();
            when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashed");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                // simulate DB assigning ID
                return User.builder().id(1L).firstName(u.getFirstName()).lastName(u.getLastName())
                        .email(u.getEmail()).password(u.getPassword()).role(u.getRole()).enabled(true).build();
            });
            when(jwtService.generateToken(anyMap(), any(User.class))).thenReturn("jwt.token.here");

            AuthResponse response = authService.register(req);

            assertThat(response.getToken()).isEqualTo("jwt.token.here");
            assertThat(response.getEmail()).isEqualTo("john.doe@test.com");
            assertThat(response.getRole()).isEqualTo(Role.USER);
            assertThat(response.getType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("new users always receive USER role, never ADMIN")
        void alwaysAssignsUserRole() {
            RegisterRequest req = TestDataFactory.buildRegisterRequest();
            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("hashed");
            when(jwtService.generateToken(anyMap(), any())).thenReturn("token");
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            AuthResponse response = authService.register(req);

            assertThat(response.getRole()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("hashes password before persisting — never stores plaintext")
        void hashesPasswordBeforeSave() {
            RegisterRequest req = TestDataFactory.buildRegisterRequest();
            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("$2a$10$BCryptHash");
            when(jwtService.generateToken(anyMap(), any())).thenReturn("token");
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            authService.register(req);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getPassword()).isEqualTo("$2a$10$BCryptHash");
            assertThat(captor.getValue().getPassword()).doesNotContain("password123");
        }

        @Test
        @DisplayName("throws EmailAlreadyExistsException for duplicate email")
        void throwsOnDuplicateEmail() {
            RegisterRequest req = TestDataFactory.buildRegisterRequest();
            when(userRepository.existsByEmail(req.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessageContaining(req.getEmail());

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("returns full name composed from first + last name")
        void returnsFullName() {
            RegisterRequest req = TestDataFactory.buildRegisterRequest();
            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("hashed");
            when(jwtService.generateToken(anyMap(), any())).thenReturn("token");
            when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            AuthResponse response = authService.register(req);

            assertThat(response.getFullName()).isEqualTo("John Doe");
        }
    }

    // ─── login ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("returns JWT on valid credentials")
        void successfulLogin() {
            User user = TestDataFactory.buildRegularUser();
            LoginRequest req = TestDataFactory.buildLoginRequest(user.getEmail(), "user123");

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(jwtService.generateToken(anyMap(), any())).thenReturn("valid.jwt.token");

            AuthResponse response = authService.login(req);

            assertThat(response.getToken()).isEqualTo("valid.jwt.token");
            assertThat(response.getEmail()).isEqualTo(user.getEmail());
            assertThat(response.getRole()).isEqualTo(Role.USER);
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("propagates BadCredentialsException from AuthenticationManager")
        void throwsOnInvalidCredentials() {
            LoginRequest req = TestDataFactory.buildLoginRequest("x@test.com", "wrong");
            doThrow(new BadCredentialsException("Bad credentials"))
                    .when(authenticationManager).authenticate(any());

            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("includes role in generated JWT claims")
        void includesRoleInClaims() {
            User user = TestDataFactory.buildAdminUser();
            LoginRequest req = TestDataFactory.buildLoginRequest(user.getEmail(), "admin123");

            when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
            when(jwtService.generateToken(anyMap(), any())).thenReturn("token");

            authService.login(req);

            ArgumentCaptor<java.util.Map<String, Object>> claimsCaptor = ArgumentCaptor.forClass(java.util.Map.class);
            verify(jwtService).generateToken(claimsCaptor.capture(), any());
            assertThat(claimsCaptor.getValue()).containsEntry("role", "ADMIN");
        }
    }
}