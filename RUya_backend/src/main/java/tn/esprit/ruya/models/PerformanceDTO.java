package tn.esprit.ruya.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PerformanceDTO {

    private LocalDateTime periodeDebut;
    private LocalDateTime periodeFin;

    // === MÉTRIQUES DE VOLUME ===
    private Long volumeRemisesTraitees;
    private Long volumeChequesTraites;
    private Long volumeCTRTraites;

    // === MÉTRIQUES DE TEMPS ===
    private Double tempsTraitementMoyenRemise; // en minutes
    private Double tempsTraitementMoyenCheque; // en minutes
    private Double tempsEquilibrageMoyenCTR; // en minutes

    // === MÉTRIQUES DE QUALITÉ ===
    private Double tauxErreurGlobal;
    private Double tauxRepriseManuelle;
    private Long nombreAnomaliesDetectees;

    // === MÉTRIQUES MÉTIER ===
    private Double montantTotalTraite;
    private Double montantMoyenParRemise;
    private Double montantMoyenParCheque;

    // === INDICATEURS DE PERFORMANCE ===
    private Double throughputRemisesParHeure;
    private Double throughputChequesParHeure;
    private Double tauxDisponibiliteSysteme;

    /**
     * Calcule le score de performance global (0-100)
     */
    public Double getScorePerformanceGlobal() {
        double scoreVolume = Math.min(100, (volumeRemisesTraitees + volumeChequesTraites) / 100.0);
        double scoreQualite = Math.max(0, 100 - (tauxErreurGlobal * 10));
        double scoreTemps = Math.max(0, 100 - (tempsTraitementMoyenRemise / 10));

        return (scoreVolume + scoreQualite + scoreTemps) / 3.0;
    }

    /**
     * Détermine le niveau de performance
     */
    public String getNiveauPerformance() {
        double score = getScorePerformanceGlobal();
        if (score >= 90) return "EXCELLENT";
        if (score >= 75) return "BON";
        if (score >= 60) return "MOYEN";
        if (score >= 45) return "FAIBLE";
        return "CRITIQUE";
    }
}