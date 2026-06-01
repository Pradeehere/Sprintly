package com.sprintly.service;

import com.sprintly.dto.request.LoginRequest;
import com.sprintly.dto.request.RefreshTokenRequest;
import com.sprintly.dto.request.RegisterRequest;
import com.sprintly.dto.response.AuthResponse;
import com.sprintly.dto.response.UserResponse;
import com.sprintly.entity.RefreshToken;
import com.sprintly.entity.User;
import com.sprintly.exception.BadRequestException;
import com.sprintly.exception.UnauthorizedException;
import com.sprintly.mapper.UserMapper;
import com.sprintly.repository.RefreshTokenRepository;
import com.sprintly.repository.UserRepository;
import com.sprintly.security.JwtTokenProvider;
import com.sprintly.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private UserMapper userMapper;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository,
                refreshTokenRepository,
                passwordEncoder,
                authenticationManager,
                jwtTokenProvider,
                userMapper,
                604800000L
        );
    }

    @Test
    void register_ShouldSaveUser_WhenEmailIsUnique() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("password");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        
        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail(request.getEmail());
        savedUser.setFullName(request.getFullName());

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = UserResponse.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .build();
        when(userMapper.toResponse(savedUser)).thenReturn(response);

        UserResponse result = authService.register(request);

        assertNotNull(result);
        assertEquals(request.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("john@example.com");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("john@example.com");
        request.setPassword("password");

        UUID userId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(userId, request.getEmail(), "hashedPassword", Collections.emptyList());
        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(principal);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        User user = new User();
        user.setId(userId);
        user.setEmail(request.getEmail());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        when(jwtTokenProvider.generateAccessToken(auth)).thenReturn("accessToken");
        when(userMapper.toResponse(user)).thenReturn(UserResponse.builder().id(userId).email(user.getEmail()).build());

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("accessToken", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void refresh_ShouldRotateTokens_WhenRefreshTokenIsValid() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("oldToken");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("john@example.com");

        RefreshToken token = new RefreshToken();
        token.setToken("oldToken");
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusDays(1));
        token.setRevoked(false);

        when(refreshTokenRepository.findByToken("oldToken")).thenReturn(Optional.of(token));
        when(jwtTokenProvider.generateAccessTokenFromUserId(user.getId())).thenReturn("newAccessToken");

        UserResponse userResponse = UserResponse.builder().id(user.getId()).email(user.getEmail()).build();
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        AuthResponse response = authService.refresh(request);

        assertNotNull(response);
        assertEquals("newAccessToken", response.getAccessToken());
        assertNotEquals("oldToken", response.getRefreshToken());
        assertTrue(token.isRevoked());
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    void refresh_ShouldThrowException_WhenTokenIsExpired() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("expiredToken");

        RefreshToken token = new RefreshToken();
        token.setToken("expiredToken");
        token.setExpiresAt(LocalDateTime.now().minusDays(1));
        token.setRevoked(false);

        when(refreshTokenRepository.findByToken("expiredToken")).thenReturn(Optional.of(token));

        assertThrows(UnauthorizedException.class, () -> authService.refresh(request));
        verify(refreshTokenRepository, times(1)).delete(token);
    }
}
