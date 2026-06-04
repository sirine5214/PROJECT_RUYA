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
@Table(name = "AMPLITUDE")
public class Amplitude {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_AMPLITUDE")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JoinColumn(name = "ID_USER", nullable = false)
    private User user;

    // Type de chèque tel qu'affiché dans le tableau (INTRA, INTER)
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE_CHEQUE", nullable = false)
    private TypeCheque typeCheque; // INTRA, INTER

    // Statut de traitement dans Amplitude
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUT_TRAITEMENT")
    private StatutTraitement statutTraitement; // INTEGRE, REJETE, EN_COURS

    @Column(name = "MONTANT")
    private Double montant;

    @Column(name = "NUMERO_CHEQUE")
    private String numeroCheque;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public enum TypeCheque { INTRA, INTER }
    public enum StatutTraitement { INTEGRE, REJETE, EN_COURS }
}


