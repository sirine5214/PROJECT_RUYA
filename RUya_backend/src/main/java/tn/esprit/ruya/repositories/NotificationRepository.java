package tn.esprit.ruya.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.ruya.models.Notification;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Récupérer toutes les notifications triées par timestamp décroissant
    List<Notification> findAllByOrderByTimestampDesc();

    // Récupérer les notifications non lues triées par timestamp décroissant
    List<Notification> findByLuOrderByTimestampDesc(Boolean lu);

    // Compter les notifications non lues
    Long countByLu(Boolean lu);

    // Récupérer les notifications d'un utilisateur triées par timestamp décroissant
    List<Notification> findByUserActionIdOrderByTimestampDesc(Long userId);

    // Récupérer les notifications non lues d'un utilisateur triées par timestamp
    // décroissant
    List<Notification> findByUserActionIdAndLuFalseOrderByTimestampDesc(Long userId);

    // Récupérer les notifications d'un fichier spécifique
    List<Notification> findByFichierIdOrderByTimestampDesc(Long fichierId);

    // Récupérer les notifications par type
    List<Notification> findByTypeOrderByTimestampDesc(Notification.NotificationType type);

    // Récupérer les notifications d'un utilisateur par type
    List<Notification> findByUserActionIdAndTypeOrderByTimestampDesc(Long userId, Notification.NotificationType type);

    // Récupérer les notifications non lues d'un utilisateur par type
    List<Notification> findByUserActionIdAndTypeAndLuFalseOrderByTimestampDesc(Long userId,
            Notification.NotificationType type);

    // Compter les notifications non lues d'un utilisateur
    Long countByUserActionIdAndLuFalse(Long userId);

    // Compter les notifications d'un utilisateur par type
    Long countByUserActionIdAndType(Long userId, Notification.NotificationType type);

    // Compter les notifications non lues d'un utilisateur par type
    Long countByUserActionIdAndTypeAndLuFalse(Long userId, Notification.NotificationType type);

    // Récupérer les notifications avant une date donnée (pour nettoyage)
    List<Notification> findByTimestampBefore(LocalDateTime timestamp);

    // Récupérer les notifications entre deux dates
    List<Notification> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);

    // Récupérer les notifications d'un utilisateur entre deux dates
    List<Notification> findByUserActionIdAndTimestampBetweenOrderByTimestampDesc(Long userId, LocalDateTime start,
            LocalDateTime end);

    // Récupérer les notifications par titre (recherche)
    List<Notification> findByTitreContainingIgnoreCaseOrderByTimestampDesc(String titre);

    // Récupérer les notifications d'un utilisateur par titre (recherche)
    List<Notification> findByUserActionIdAndTitreContainingIgnoreCaseOrderByTimestampDesc(Long userId, String titre);

    // Récupérer les notifications par message (recherche)
    List<Notification> findByMessageContainingIgnoreCaseOrderByTimestampDesc(String message);

    // Récupérer les notifications d'un utilisateur par message (recherche)
    List<Notification> findByUserActionIdAndMessageContainingIgnoreCaseOrderByTimestampDesc(Long userId,
            String message);

    // Récupérer les notifications par icône
    List<Notification> findByIconOrderByTimestampDesc(String icon);

    // Récupérer les notifications d'un utilisateur par icône
    List<Notification> findByUserActionIdAndIconOrderByTimestampDesc(Long userId, String icon);
}
