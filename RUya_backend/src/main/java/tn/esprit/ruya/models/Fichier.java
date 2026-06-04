package tn.esprit.ruya.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "FICHIERS")
public class Fichier extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    @JoinColumn(name = "ID_USER", nullable = false)
    @ToString.Exclude
    private User user;

    // === VARIABLES PRINCIPALES ===
    @Column(name = "NOM_FICHIER", nullable = false)
    private String nomFichier;

    @Column(name = "TYPE_FICHIER")
    @Enumerated(EnumType.STRING)
    private TypeFichier typeFichier;

    @Column(name = "NATURE_FICHIER")
    @Enumerated(EnumType.STRING)
    private NatureFichier natureFichier;

    @Column(name = "CODE_VALEUR")
    private String codeValeur;

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
    @Column(name = "ORIGINE_SAISIE")
    @Enumerated(EnumType.STRING)
    private OrigineSaisie origineSaisie;

    @Column(name = "VALIDATION")
    private Boolean validation;

    @Column(name = "DATE_VALIDATION")
    private LocalDateTime dateValidation;

    @Column(name = "GENERE_PAR_ENCAISSE")
    private Boolean genereParEncaisse;

    @Column(name = "NUMERO_REMISE")
    private String numeroRemise;

    @Column(name = "TYPE_ENCAISSEMENT")
    @Enumerated(EnumType.STRING)
    private TypeEncaissement typeEncaissement;

    // === ENUMS ===
    public enum TypeFichier {
        WEB, ELECTRONIQUE, MANUEL, EN_SAISIE
    }

    public enum NatureFichier {
        REMISE, FICHIER
    }

    public enum Sens {
        ENTRANT, SORTANT
    }

    public enum OrigineSaisie {
        WEB, AGENCE, BATCH
    }

    public enum TypeEncaissement {
        IMMEDIAT, DIFFERE
    }

    // Types de valeur (codeValeur)
    public enum CodeValeur {
        CHEQUE,
        EFFET,
        PRELEVEMENT,
        VIREMENT
    }
}