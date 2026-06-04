package tn.esprit.ruya.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SyntheseDTO {

    // === STATISTIQUES FICHIERS ===
    private Long totalRemises;
    private Long remisesValidees;
    private Long remisesWeb;
    private Long remisesEnCours;

    // === STATISTIQUES CARTHAGO ===
    private Long totalCheques;
    private Long chequesTraites;
    private Long chequesAVerifier;
    private Long chequesElectroniques;
    private Long chequesManuels;

    // === STATISTIQUES CTR ===
    private Long totalCTR;
    private Long ctrEquilibres;
    private Long remisesDouble;
    private Long remisesNonParvenues;

    // === TAUX CALCULÉS ===
    private Double tauxValidationRemises;
    private Double tauxTraitementCheques;
    private Double tauxEquilibrageCTR;
    private Double tauxGlobalReussite;

    // === MONTANTS ===
    private Double montantTotalRemises;
    private Double montantTotalCheques;
    private Double montantTotalCTR;

    /**
     * Calcule automatiquement les taux basés sur les données
     */
    public void calculerTaux() {
        // Taux de validation des remises
        this.tauxValidationRemises = totalRemises > 0 ?
                (remisesValidees * 100.0) / totalRemises : 0.0;

        // Taux de traitement des chèques
        this.tauxTraitementCheques = totalCheques > 0 ?
                (chequesTraites * 100.0) / totalCheques : 0.0;

        // Taux d'équilibrage CTR
        this.tauxEquilibrageCTR = totalCTR > 0 ?
                (ctrEquilibres * 100.0) / totalCTR : 0.0;

        // Taux global de réussite
        this.tauxGlobalReussite = (tauxValidationRemises + tauxTraitementCheques + tauxEquilibrageCTR) / 3.0;
    }

    /**
     * Détermine le statut global basé sur les taux
     */
    public String getStatutGlobal() {
        if (tauxGlobalReussite >= 95) {
            return "EXCELLENT";
        } else if (tauxGlobalReussite >= 80) {
            return "BON";
        } else if (tauxGlobalReussite >= 60) {
            return "MOYEN";
        } else {
            return "CRITIQUE";
        }
    }

    /**
     * Identifie le goulot d'étranglement principal
     */
    public String getGoulotEtranglement() {
        if (tauxValidationRemises < tauxTraitementCheques && tauxValidationRemises < tauxEquilibrageCTR) {
            return "VALIDATION_REMISES";
        } else if (tauxTraitementCheques < tauxEquilibrageCTR) {
            return "TRAITEMENT_CHEQUES";
        } else {
            return "EQUILIBRAGE_CTR";
        }
    }
}