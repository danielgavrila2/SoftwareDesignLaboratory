package com.fitness.userservice.services;

import com.fitness.userservice.PasswordResetTokenRepository;
import com.fitness.userservice.UserRepository;
import com.fitness.userservice.models.PasswordResetToken;
import com.fitness.userservice.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;

    @Transactional
    public void createPasswordResetTokenForUser(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }

        // Delete existing token if any
        Optional<PasswordResetToken> existingToken = tokenRepository.findByUser(user);
        existingToken.ifPresent(tokenRepository::delete);

        String token = UUID.randomUUID().toString();
        PasswordResetToken myToken = new PasswordResetToken(token, user);
        tokenRepository.save(myToken);

        String resetUrl = "http://localhost:5173/reset-password?token=" + token;
        String message = "To reset your password, click the link below:\n" + resetUrl;
        
        emailService.sendSimpleMessage(email, "Password Reset Request", message);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.isExpired()) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Token expired");
        }

        User user = resetToken.getUser();
        user.setPassword(newPassword); // In a real app, hash this!
        userRepository.save(user);

        tokenRepository.delete(resetToken);
    }
}
