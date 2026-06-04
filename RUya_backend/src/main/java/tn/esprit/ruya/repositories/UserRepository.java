package tn.esprit.ruya.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.ruya.models.User;
import tn.esprit.ruya.models.RoleUser;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Rechercher un utilisateur par email
    Optional<User> findByEmail(String email);

    // Rechercher un utilisateur par nom d'utilisateur
    Optional<User> findByUsername(String username);

    // Vérifier si un email existe
    boolean existsByEmail(String email);

    // Vérifier si un nom d'utilisateur existe
    boolean existsByUsername(String username);

    // Rechercher des utilisateurs par rôle
    List<User> findByRole(RoleUser role);

    // Rechercher des utilisateurs actifs
    List<User> findByIsActiveTrue();

    // Rechercher des utilisateurs inactifs
    List<User> findByIsActiveFalse();

    // Rechercher des utilisateurs par nom d'utilisateur contenant une chaîne
    List<User> findByUsernameContainingIgnoreCase(String username);

    // Rechercher des utilisateurs par email contenant une chaîne
    List<User> findByEmailContainingIgnoreCase(String email);

    // Rechercher des utilisateurs par nom d'utilisateur ou email contenant une chaîne
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> findByUsernameOrEmailContainingIgnoreCase(@Param("searchTerm") String searchTerm);

    // Compter les utilisateurs par rôle
    Long countByRole(RoleUser role);

    // Compter les utilisateurs actifs
    Long countByIsActiveTrue();

    // Compter les utilisateurs inactifs
    Long countByIsActiveFalse();

    // Rechercher des utilisateurs créés après une date donnée
    List<User> findByCreatedAtAfter(java.time.LocalDateTime date);

    // Rechercher des utilisateurs créés entre deux dates
    List<User> findByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);

    // Rechercher des utilisateurs par rôle et statut actif
    List<User> findByRoleAndIsActiveTrue(RoleUser role);

    // Rechercher des utilisateurs par rôle et statut inactif
    List<User> findByRoleAndIsActiveFalse(RoleUser role);
}
