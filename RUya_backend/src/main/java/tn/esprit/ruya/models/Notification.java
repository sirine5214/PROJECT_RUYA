package tn.esprit.ruya.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "NOTIFICATIONS")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_NOTIFICATION")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false)
    private NotificationType type;

    @Column(name = "TITRE", nullable = false)
    private String titre;

    @Column(name = "MESSAGE", nullable = false)
    private String message;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "ID_FICHIER")
    private Fichier fichier;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "ID_USER_ACTION")
    private User userAction; // L'utilisateur qui a effectué l'action

    @Column(name = "TIMESTAMP", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "LU", nullable = false)
    private Boolean lu = false;

    @Column(name = "ICON")
    private String icon;

    @PrePersist
    private void prePersist() {
        this.timestamp = LocalDateTime.now();
    }

    public enum NotificationType {
        AJOUT, ENVOI, RECEPTION
    }
} 