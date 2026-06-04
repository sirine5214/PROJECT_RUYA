package tn.esprit.ruya.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.ruya.models.Notification;

import java.util.List;

@Repository
public interface INotificationRepo extends JpaRepository<Notification, Long> {

    // Récupérer toutes les notifications ordonnées par timestamp
    List<Notification> findAllByOrderByTimestampDesc();

    // Récupérer toutes les notifications non lues
    List<Notification> findByLuOrderByTimestampDesc(Boolean lu);

    // Récupérer toutes les notifications par type
    List<Notification> findByTypeOrderByTimestampDesc(Notification.NotificationType type);

    // Compter les notifications non lues
    Long countByLu(Boolean lu);

    // Récupérer les notifications d'un utilisateur triées par timestamp décroissant
    List<Notification> findByUserActionIdOrderByTimestampDesc(Long userId);

    // Récupérer les notifications non lues d'un utilisateur triées par timestamp
    // décroissant
    List<Notification> findByUserActionIdAndLuFalseOrderByTimestampDesc(Long userId);

    // Compter les notifications non lues d'un utilisateur
    Long countByUserActionIdAndLuFalse(Long userId);
}