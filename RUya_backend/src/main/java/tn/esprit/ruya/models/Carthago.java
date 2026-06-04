package tn.esprit.ruya.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "CARTHAGO")
public class Carthago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CARTHAGO")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @JoinColumn(name = "ID_USER", nullable = false)
    @ToString.Exclude
    private User user;

    // === VARIABLES PRINCIPALES ===
    @Column(name = "NOM_FICHIER", nullable = false)
    private String nomFichier;

    @Column(name = "TYPE_FICHIER", nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeFichier typeFichier;

    @Column(name = "NATURE_FICHIER", nullable = false)
    @Enumerated(EnumType.STRING)
    private NatureFichier natureFichier;

    @Column(name = "CODE_VALEUR", nullable = false)
    @Enumerated(EnumType.STRING)
    private CodeValeur codeValeur;

    @Column(name = "COD_EN")
    private String codEn;

    @Column(name = "SENS")
    @Enumerated(EnumType.STRING)
    private Sens sens;

    @Column(name = "MONTANT")
    private Double montant;

    @Column(name = "NOMBER")
    private Integer nomber;

    // === VARIABLES DASHBOARD ===
    @Column(name = "SESSION_DATE")
    private LocalDate sessionDate;

    @Column(name = "STATUT_CHEQUE")
    @Enumerated(EnumType.STRING)
    private StatutCheque statutCheque;

    @Column(name = "NUMERO_CHEQUE")
    private String numeroCheque;

    @Column(name = "BANQUE_EMETTRICE")
    private String banqueEmettrice;

    @Column(name = "AVANT_CTR")
    private Boolean avantCTR;

    @Column(name = "APRES_CTR")
    private Boolean apresCTR;

    @Column(name = "STATUT_IMAGE")
    private Integer statutImage;

    @Column(name = "TRAITE_PAR_CTR")
    private Boolean traiteParCTR;

    @Column(name = "DATE_TRAITEMENT_CTR")
    private LocalDateTime dateTraitementCTR;

    @Column(name = "VALIDE_BO_DINARS")
    private Boolean valideBodinars;

    @Column(name = "CHEQUE_WEB_BO")
    private Boolean chequeWebBo;

    @Column(name = "REMISE_DOUBLE")
    private Boolean remiseDouble;

    @Column(name = "REFERENCE_ORIGINALE")
    private String referenceOriginale;

    @Column(name = "FICHIER_ENV")
    private Boolean fichierEnv;

    @Column(name = "CODE_ENV")
    private String codeEnv;

    @Column(name = "A_VERIFIER")
    private Boolean aVerifier;

    @Column(name = "CONTROLE_EFFECTUE")
    private Boolean controleEffectue;

    @Column(name = "DATE_CONTROLE")
    private LocalDateTime dateControle;

    // === TIMESTAMPS ===
    @CreationTimestamp
    @Column(name = "CREATED_AT", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    // === ENUMS ===
    public enum TypeFichier {
        ELECTRONIQUE, MANUEL, WEB
    }

    public enum NatureFichier {
        FICHIER, REMISE
    }

    public enum Sens {
        ENTRANT, SORTANT
    }

    // Types de valeur (codeValeur) - Types de documents bancaires
    public enum CodeValeur {
        CHEQUE, // Chèques
        EFFET, // Effets de commerce
        PRELEVEMENT, // Prélèvements
        VIREMENT // Virements
    }

    // Statuts des chèques
    public enum StatutCheque {
        TRAITE, EN_COURS, REJETE, PENDING
    }
}
