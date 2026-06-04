package tn.esprit.ruya.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ruya.models.Notification;
import tn.esprit.ruya.services.NotificationService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationController {

    private NotificationService notificationService;

    // Récupérer toutes les notifications
    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    // Récupérer toutes les notifications d'un utilisateur
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationsByUser(userId));
    }

    // Récupérer les notifications non lues
    @GetMapping("/non-lues")
    public ResponseEntity<List<Notification>> getNotificationsNonLues() {
        return ResponseEntity.ok(notificationService.getNotificationsNonLues());
    }

    // Récupérer les notifications non lues d'un utilisateur
    @GetMapping("/user/{userId}/non-lues")
    public ResponseEntity<List<Notification>> getNotificationsNonLuesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getNotificationsNonLuesByUser(userId));
    }

    // Marquer une notification comme lue
    @PutMapping("/{id}/marquer-lue")
    public ResponseEntity<Notification> marquerCommeLue(@PathVariable Long id) {
        Notification notification = notificationService.marquerCommeLue(id);
        if (notification != null) {
            return ResponseEntity.ok(notification);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Marquer toutes les notifications comme lues
    @PutMapping("/marquer-toutes-lues")
    public ResponseEntity<Void> marquerToutesCommeLues() {
        notificationService.marquerToutesCommeLues();
        return ResponseEntity.ok().build();
    }

    // Compter les notifications non lues
    @GetMapping("/count-non-lues")
    public ResponseEntity<Long> countNotificationsNonLues() {
        return ResponseEntity.ok(notificationService.countNotificationsNonLues());
    }

    // Compter les notifications non lues d'un utilisateur
    @GetMapping("/user/{userId}/count-non-lues")
    public ResponseEntity<Long> countNotificationsNonLuesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.countNotificationsNonLuesByUser(userId));
    }
}