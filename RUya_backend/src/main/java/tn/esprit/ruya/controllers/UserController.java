package tn.esprit.ruya.controllers;

import lombok.RequiredArgsConstructor;
import tn.esprit.ruya.models.ConfirmResetCodeDto;
import tn.esprit.ruya.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ruya.services.UserServ;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private final UserServ userServ;
    private final PasswordEncoder passwordEncoder; // Add this field

    // ✅ Get all users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userServ.getAllUsers());
    }

    // ✅ Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userServ.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Create new user
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        System.out.println("Tentative de création d'utilisateur : " + user.getUsername());

        boolean usernameExists = userServ.existsByUsername(user.getUsername());
        boolean emailExists = userServ.existsByEmail(user.getEmail());

        if (usernameExists || emailExists) {
            String msg = "Utilisateur déjà existant avec : " +
                    (usernameExists ? "username " : "") +
                    (usernameExists && emailExists ? "et " : "") +
                    (emailExists ? "email" : "");
            return ResponseEntity.badRequest().body(Map.of("error", msg));
        }

        User createdUser = userServ.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        System.out.println("Tentative login : " + loginRequest.getUsername());
        Optional<User> userOpt = userServ.findByUsernameOrEmail(loginRequest.getUsername());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            System.out.println("Utilisateur trouvé : " + user.getUsername());

            // ✅ FIXED: Use passwordEncoder.matches() to compare passwords
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()) && user.getIsActive()) {
                String token = generateMockToken(user.getUsername());
                return ResponseEntity.ok().body(Map.of("token", token, "user", user));
            } else {
                System.out.println("❌ Mot de passe incorrect ou compte inactif");
            }
        } else {
            System.out.println("❌ Utilisateur non trouvé");
        }

        return ResponseEntity.status(401).body(Map.of("error", "Identifiants invalides"));
    }

    private String generateMockToken(String username) {
        return Base64.getEncoder().encodeToString((username + ":" + System.currentTimeMillis()).getBytes());
    }

    // ✅ Update user by ID
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        User user = userServ.updateUser(id, updatedUser);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ Delete user by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userServ.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        return userServ.generateAndSendResetCode(email);
    }

    @PostMapping("/confirm-reset-password")
    public ResponseEntity<?> confirmResetPassword(@RequestBody ConfirmResetCodeDto dto) {
        return userServ.confirmResetCode(dto);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<User> updateUserStatus(@PathVariable Long id, @RequestParam boolean active) {
        try {
            User updatedUser = userServ.updateUserStatus(id, active);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ Update user profile (for both ADMIN and USER)
    @PutMapping("/{id}/profile")
    public ResponseEntity<?> updateProfile(@PathVariable Long id, @RequestBody Map<String, String> profileData) {
        try {
            User updatedUser = userServ.updateProfile(id, profileData);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Change password
    @PutMapping("/{id}/change-password")
    public ResponseEntity<?> changePassword(@PathVariable Long id, @RequestBody Map<String, String> passwordData) {
        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");

        try {
            userServ.changePassword(id, currentPassword, newPassword);
            return ResponseEntity.ok(Map.of("message", "Mot de passe modifié avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ✅ Get current user profile (by token/session)
    @GetMapping("/profile/me")
    public ResponseEntity<?> getCurrentUserProfile(@RequestParam Long userId) {
        try {
            Optional<User> user = userServ.getUserById(userId);
            return user.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}