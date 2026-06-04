package tn.esprit.ruya.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class DashboardConfigDTO {

    // === CONFIGURATION REFRESH ===
    private Integer intervalleRefreshSecondes = 30;
    private boolean autoRefreshActive = true;

    // === SEUILS D'ALERTE ===
    private Map<String, Object> seuils;

    // === HEURES D'ACTIVITÉ ===
    private LocalTime heureDebutActivite = LocalTime.of(8, 0);
    private LocalTime heureFinActivite = LocalTime.of(18, 0);

    // === CONFIGURATION AFFICHAGE ===
    private List<String> cartesVisibles;
    private boolean affichageMontantsActive = true;
    private boolean alertesSonoresActives = false;

    // === FILTRES ===
    private List<String> utilisateursAutorises;
    private List<String> typesOperationsAffiches;

    /**
     * Initialise la configuration par défaut
     */
    public void initialiserConfigurationDefaut() {
        // Seuils par défaut
        seuils = Map.of(
                "MAX_CHEQUES_A_VERIFIER", 10,
                "MAX_REMISES_NON_PARVENUES", 5,
                "MIN_TAUX_REUSSITE", 85.0,
                "MAX_IMAGES_STATUT3", 3
        );

        // Cartes visibles par défaut
        cartesVisibles = List.of(
                "CLIENT_EXTERNE",
                "SESSION_DU_JOUR",
                "F_GENERER",
                "CLIENT_EXTERNE_WEB",
                "FICHIERS_CARTHAGO",
                "VALIDATION_BO",
                "CARTHAGO_AVANT_CTR",
                "REMISES_CTR",
                "ACTIONS_CONTROLES"
        );
    }

    /**
     * Vérifie si une alerte doit être déclenchée
     */
    public boolean doitDeclencherAlerte(String typeMetrique, Object valeur) {
        if (!seuils.containsKey(typeMetrique)) {
            return false;
        }

        Object seuil = seuils.get(typeMetrique);

        if (valeur instanceof Number && seuil instanceof Number) {
            double val = ((Number) valeur).doubleValue();
            double seuilVal = ((Number) seuil).doubleValue();

            // Logique selon le type de métrique
            switch (typeMetrique) {
                case "MIN_TAUX_REUSSITE":
                    return val < seuilVal;
                case "MAX_CHEQUES_A_VERIFIER":
                case "MAX_REMISES_NON_PARVENUES":
                case "MAX_IMAGES_STATUT3":
                    return val > seuilVal;
                default:
                    return false;
            }
        }

        return false;
    }
}