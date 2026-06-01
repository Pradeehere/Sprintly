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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@SuppressWarnings("null")
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;
    private final long refreshExpirationMs;

    public AuthServiceImpl(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            UserMapper userMapper,
            @Value("${sprintly.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userMapper = userMapper;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        User user = new User();
        user.setFullName(registerRequest.getFullName());
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setVerified(false);

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new BadRequestException("User not found"));

        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshTokenString = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(refreshTokenString);
        refreshToken.setExpiresAt(LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000));
        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenString)
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest refreshRequest) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshRequest.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (token.isRevoked()) {
            refreshTokenRepository.deleteByUserId(token.getUser().getId());
            throw new UnauthorizedException("Refresh token has been revoked (reuse detected). All sessions ended.");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new UnauthorizedException("Refresh token has expired");
        }

        token.setRevoked(true);
        refreshTokenRepository.save(token);

        String newAccessToken = jwtTokenProvider.generateAccessTokenFromUserId(token.getUser().getId());
        String newRefreshTokenString = UUID.randomUUID().toString();

        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setUser(token.getUser());
        newRefreshToken.setToken(newRefreshTokenString);
        newRefreshToken.setExpiresAt(LocalDateTime.now().plusNanos(refreshExpirationMs * 1_000_000));
        newRefreshToken.setRevoked(false);

        refreshTokenRepository.save(newRefreshToken);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenString)
                .user(userMapper.toResponse(token.getUser()))
                .build();
    }

    @Override
    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
