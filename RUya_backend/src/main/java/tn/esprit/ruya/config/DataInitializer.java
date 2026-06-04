package tn.esprit.ruya.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import tn.esprit.ruya.models.RoleUser;
import tn.esprit.ruya.models.User;
import tn.esprit.ruya.repositories.IUserRepo;

import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    @Bean
    @Order(1) // Exécuté en premier
    CommandLineRunner initDatabase(IUserRepo userRepo) {
        return args -> {
            // Vérifier si des utilisateurs existent déjà
            if (userRepo.count() == 0) {
                System.out.println("🔄 Initialisation des utilisateurs...");
                
                // Créer l'utilisateur admin
                User admin = new User();
                admin.setUsername("admin");
                admin.setEmail("admin@ruya.com");
                admin.setPassword(passwordEncoder.encode("password"));
                admin.setRole(RoleUser.ADMIN);
                admin.setIsActive(true);
                admin.setCreatedAt(LocalDateTime.now());
                userRepo.save(admin);
                
                // Créer l'utilisateur user1
                User user1 = new User();
                user1.setUsername("user1");
                user1.setEmail("user1@ruya.com");
                user1.setPassword(passwordEncoder.encode("password"));
                user1.setRole(RoleUser.SIMPLE_USER);
                user1.setIsActive(true);
                user1.setCreatedAt(LocalDateTime.now());
                userRepo.save(user1);
                
                System.out.println("✅ Utilisateurs initialisés avec succès!");
            } else {
                System.out.println("ℹ️ Des utilisateurs existent déjà, pas d'initialisation nécessaire.");
            }
        };
    }
}