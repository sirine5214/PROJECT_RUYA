package tn.esprit.ruya.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AnomalieDTO {

    private String code;
    private String description;
    private String niveau; // CRITIQUE, ALERTE, ATTENTION, INFO
    private LocalDateTime dateDetection;
    private String action; // Action recommandée
    private Object valeurDetectee;
    private Object seuilDeReference;

    public AnomalieDTO(String code, String description, String niveau) {
        this.code = code;
        this.description = description;
        this.niveau = niveau;
        this.dateDetection = LocalDateTime.now();
    }

    /**
     * Détermine la priorité numérique de l'anomalie
     */
    public int getPriorite() {
        switch (niveau) {
            case "CRITIQUE": return 1;
            case "ALERTE": return 2;
            case "ATTENTION": return 3;
            case "INFO": return 4;
            default: return 5;
        }
    }

    /**
     * Génère une action recommandée basée sur le code d'anomalie
     */
    public String getActionRecommandee() {
        switch (code) {
            case "EQUILIBRAGE_NOMBRE":
                return "Vérifier la cohérence entre Carthago, Fichiers et CTR";
            case "EQUILIBRAGE_MONTANT":
                return "Controller les montants et identifier les écarts";
            case "REMISES_NON_PARVENUES":
                return "Investiguer les remises en attente et relancer si nécessaire";
            case "IMAGES_STATUT3":
                return "Traiter les images bloquées en statut 3";
            case "CHEQUES_A_VERIFIER":
                return "Procéder à la vérification manuelle des chèques";
            default:
                return "Analyser l'anomalie et prendre les mesures appropriées";
        }
    }
}