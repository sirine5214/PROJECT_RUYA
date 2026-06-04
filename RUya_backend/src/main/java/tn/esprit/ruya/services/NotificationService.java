package tn.esprit.ruya.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.ruya.models.Fichier;
import tn.esprit.ruya.models.Notification;
import tn.esprit.ruya.models.User;
import tn.esprit.ruya.repositories.INotificationRepo;

import java.util.List;

@AllArgsConstructor
@Service
public class NotificationService {

    private INotificationRepo notificationRepo;

    // Créer une notification pour l'ajout d'un fichier
    public Notification creerNotificationAjout(Fichier fichier, User userAction) {
        System.out.println("🔍 DEBUG - Création de notification d'ajout pour fichier: " + fichier.getNomFichier());
        System.out.println("🔍 DEBUG - Utilisateur qui a ajouté: " + userAction.getUsername());

        String titre = "Nouveau fichier ajouté";
        String message = "Le fichier \"" + fichier.getNomFichier() + "\" a été ajouté par " + userAction.getUsername()
                + ".";
        String icon = getNotificationIcon(Notification.NotificationType.AJOUT);

        Notification notification = new Notification();
        notification.setType(Notification.NotificationType.AJOUT);
        notification.setTitre(titre);
        notification.setMessage(message);
        notification.setFichier(fichier);
        notification.setUserAction(userAction);
        notification.setIcon(icon);
        notification.setLu(false);

        Notification savedNotification = notificationRepo.save(notification);
        System.out.println("🔍 DEBUG - Notification créée avec ID: " + savedNotification.getId());

        return savedNotification;
    }

    // Récupérer toutes les notifications
    public List<Notification> getAllNotifications() {
        return notificationRepo.findAllByOrderByTimestampDesc();
    }

    // Récupérer toutes les notifications d'un utilisateur
    public List<Notification> getNotificationsByUser(Long userId) {
        return notificationRepo.findByUserActionIdOrderByTimestampDesc(userId);
    }

    // Récupérer les notifications non lues
    public List<Notification> getNotificationsNonLues() {
        return notificationRepo.findByLuOrderByTimestampDesc(false);
    }

    // Récupérer les notifications non lues d'un utilisateur
    public List<Notification> getNotificationsNonLuesByUser(Long userId) {
        return notificationRepo.findByUserActionIdAndLuFalseOrderByTimestampDesc(userId);
    }

    // Marquer une notification comme lue
    public Notification marquerCommeLue(Long notificationId) {
        return notificationRepo.findById(notificationId).map(notification -> {
            notification.setLu(true);
            return notificationRepo.save(notification);
        }).orElse(null);
    }

    // Marquer toutes les notifications comme lues
    public void marquerToutesCommeLues() {
        List<Notification> notificationsNonLues = getNotificationsNonLues();
        notificationsNonLues.forEach(notification -> {
            notification.setLu(true);
            notificationRepo.save(notification);
        });
    }

    // Compter les notifications non lues
    public Long countNotificationsNonLues() {
        return notificationRepo.countByLu(false);
    }

    // Compter les notifications non lues d'un utilisateur
    public Long countNotificationsNonLuesByUser(Long userId) {
        return notificationRepo.countByUserActionIdAndLuFalse(userId);
    }

    // Obtenir l'icône selon le type de notification
    private String getNotificationIcon(Notification.NotificationType type) {
        if (type == null) {
            return "ti ti-file";
        }

        switch (type) {
            case AJOUT:
                return "ti ti-plus";
            case ENVOI:
                return "ti ti-send";
            case RECEPTION:
                return "ti ti-inbox";
            default:
                return "ti ti-file";
        }
    }

    // Créer une notification pour l'envoi d'un fichier
    public Notification creerNotificationEnvoi(Fichier fichier, User userAction) {
        System.out.println("🔍 DEBUG - Création de notification d'envoi pour fichier: " + fichier.getNomFichier());
        System.out.println("🔍 DEBUG - Utilisateur qui a envoyé: " + userAction.getUsername());

        String titre = "Fichier envoyé";
        String message = "Le fichier \"" + fichier.getNomFichier() + "\" a été envoyé par " + userAction.getUsername()
                + ".";
        String icon = getNotificationIcon(Notification.NotificationType.ENVOI);

        Notification notification = new Notification();
        notification.setType(Notification.NotificationType.ENVOI);
        notification.setTitre(titre);
        notification.setMessage(message);
        notification.setFichier(fichier);
        notification.setUserAction(userAction);
        notification.setIcon(icon);
        notification.setLu(false);

        Notification savedNotification = notificationRepo.save(notification);
        System.out.println("🔍 DEBUG - Notification d'envoi créée avec ID: " + savedNotification.getId());

        return savedNotification;
    }

    // Créer une notification pour la réception d'un fichier
    public Notification creerNotificationReception(Fichier fichier, User userAction) {
        System.out.println("🔍 DEBUG - Création de notification de réception pour fichier: " + fichier.getNomFichier());
        System.out.println("🔍 DEBUG - Utilisateur qui a reçu: " + userAction.getUsername());

        String titre = "Fichier reçu";
        String message = "Le fichier \"" + fichier.getNomFichier() + "\" a été reçu par " + userAction.getUsername()
                + ".";
        String icon = getNotificationIcon(Notification.NotificationType.RECEPTION);

        Notification notification = new Notification();
        notification.setType(Notification.NotificationType.RECEPTION);
        notification.setTitre(titre);
        notification.setMessage(message);
        notification.setFichier(fichier);
        notification.setUserAction(userAction);
        notification.setIcon(icon);
        notification.setLu(false);

        Notification savedNotification = notificationRepo.save(notification);
        System.out.println("🔍 DEBUG - Notification de réception créée avec ID: " + savedNotification.getId());

        return savedNotification;
    }
}