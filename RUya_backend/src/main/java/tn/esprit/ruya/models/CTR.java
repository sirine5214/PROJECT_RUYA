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
@Table(name = "CTR")
public class CTR {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CTR")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @JoinColumn(name = "ID_USER", nullable = false)
    private User user;

    // === VARIABLES PRINCIPALES CTR ===
    @Column(name = "NUMERO_CTR", nullable = false)
    private String numeroCtr; // numéro unique CTR

    @Column(name = "TYPE_OPERATION")
    private String typeOperation; // ENVOI, RECEPTION, TRAITEMENT

    @Column(name = "STATUT_CTR")
    private String statutCtr; // RECU, EN_COURS, TRAITE, EQUILIBRE, FAUX

    @Column(name = "CODE_VALEUR")
    private String codeValeur; // CTR, ENV, DOUBLE

    @Column(name = "MONTANT")
    private Double montant;

    @Column(name = "NOMBRE_ELEMENTS")
    private Integer nombreElements;

    // === VARIABLES POUR ÉQUILIBRAGE ===
    @Column(name = "NOMBRE_CARTHAGO")
    private Integer nombreCarthago; // éléments venant de Carthago

    @Column(name = "NOMBRE_FICHIERS")
    private Integer nombreFichiers; // éléments venant des Fichiers

    @Column(name = "MONTANT_CARTHAGO")
    private Double montantCarthago;

    @Column(name = "MONTANT_FICHIERS")
    private Double montantFichiers;

    @Column(name = "EQUILIBRE")
    private Boolean equilibre; // true si Carthago + Fichiers = CTR

    @Column(name = "DIFFERENCE")
    private Double difference; // écart éventuel

    // === VARIABLES DE GÉNÉRATION VERS CTR ===
    @Column(name = "GENERE_VERS_CTR")
    private Boolean genereVersCtr; // généré par Carthago vers CTR

    @Column(name = "RECU_PAR_CTR")
    private Boolean recuParCtr; // reçu et intégré par CTR

    @Column(name = "DATE_GENERATION")
    private LocalDateTime dateGeneration;

    @Column(name = "DATE_RECEPTION")
    private LocalDateTime dateReception;

    // === VARIABLES POUR REMISES CTR ===
    @Column(name = "REMISE_DOUBLE")
    private Boolean remiseDouble; // détection des doublons

    @Column(name = "REMISE_NON_PARVENUE")
    private Boolean remiseNonParvenue; // remise non reçue

    @Column(name = "CHEQUE_ELECTRONIQUE_CTR")
    private Boolean chequeElectroniqueCtr;

    @Column(name = "FICHIER_ENV_CTR")
    private Boolean fichierEnvCtr;

    // === VARIABLES DE SUIVI ET CONTRÔLE ===
    @Column(name = "SESSION_CTR")
    private String sessionCtr; // session de traitement CTR

    @Column(name = "OPERATEUR_CTR")
    private String operateurCtr; // qui a traité

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @Column(name = "DATE_TRAITEMENT")
    private LocalDateTime dateTraitement;

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // === ENUMS ===
    // Types de valeur (codeValeur) - Types de documents bancaires
    public enum CodeValeur {
        CHEQUE, // Chèques
        EFFET, // Effets de commerce
        PRELEVEMENT, // Prélèvements
        VIREMENT // Virements
    }
}