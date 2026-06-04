package tn.esprit.ruya.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EquilibrageResultDTO {

    // Données Carthago
    private Long nombreCarthago;
    private Double montantCarthago;

    // Données Fichiers
    private Long nombreFichiers;
    private Double montantFichiers;

    // Données CTR
    private Long nombreCTR;
    private Double montantCTR;

    // Résultats d'équilibrage
    private boolean equilibreNombre;
    private boolean equilibreMontant;
    private Double difference;

    // Getters calculés
    public Long getTotalCarFich() {
        return nombreCarthago + nombreFichiers;
    }

    public Double getTotalMontantCarFich() {
        return montantCarthago + montantFichiers;
    }

    public boolean isEquilibreTotal() {
        return equilibreNombre && equilibreMontant;
    }

    public String getStatutEquilibrage() {
        if (isEquilibreTotal()) {
            return "EQUILIBRE";
        } else if (equilibreNombre && !equilibreMontant) {
            return "NOMBRE_OK_MONTANT_KO";
        } else if (!equilibreNombre && equilibreMontant) {
            return "MONTANT_OK_NOMBRE_KO";
        } else {
            return "DESEQUILIBRE_TOTAL";
        }
    }
}

