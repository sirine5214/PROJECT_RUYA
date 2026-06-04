package tn.esprit.ruya.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ruya.models.CTR;
import tn.esprit.ruya.repositories.CtrRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ctr")
@CrossOrigin(
        origins = {"http://localhost:4200", "http://127.0.0.1:4200"},
        maxAge = 3600,
        allowCredentials = "false"
)
public class CtrController {

    @Autowired
    private CtrRepository ctrRepository;

    /**
     * Crée un nouveau CTR
     * @param ctr Données du CTR à créer
     * @return Le CTR créé
     */
    @PostMapping
    public ResponseEntity<CTR> createCtr(@RequestBody CTR ctr) {
        try {
            // Vérification des champs obligatoires
            if (ctr.getNumeroCtr() == null || ctr.getNumeroCtr().isEmpty()) {
                return ResponseEntity.badRequest().body(null);
            }
            CTR savedCtr = ctrRepository.save(ctr);
            return ResponseEntity.ok(savedCtr);
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du CTR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère un CTR par son ID
     * @param id ID du CTR
     * @return Le CTR correspondant ou erreur 404 si non trouvé
     */
    @GetMapping("/{id}")
    public ResponseEntity<CTR> getCtrById(@PathVariable Long id) {
        try {
            Optional<CTR> ctr = ctrRepository.findById(id);
            if (ctr.isPresent()) {
                return ResponseEntity.ok(ctr.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération du CTR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère tous les CTR dans une période donnée
     * @param start Date de début (optionnel, format ISO)
     * @param end Date de fin (optionnel, format ISO)
     * @return Liste des CTR
     */
    @GetMapping
    public ResponseEntity<List<CTR>> getAllCtr(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        try {
            LocalDateTime startDate = start != null ? LocalDateTime.parse(start) : LocalDateTime.now().minusDays(1);
            LocalDateTime endDate = end != null ? LocalDateTime.parse(end) : LocalDateTime.now();
            List<CTR> ctrList = ctrRepository.findAllByCreatedAtBetween(startDate, endDate);
            return ResponseEntity.ok(ctrList);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des CTR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Met à jour un CTR existant
     * @param id ID du CTR à mettre à jour
     * @param updatedCtr Données mises à jour
     * @return Le CTR mis à jour ou erreur 404 si non trouvé
     */
    @PutMapping("/{id}")
    public ResponseEntity<CTR> updateCtr(@PathVariable Long id, @RequestBody CTR updatedCtr) {
        try {
            Optional<CTR> existingCtr = ctrRepository.findById(id);
            if (existingCtr.isPresent()) {
                CTR ctr = existingCtr.get();
                // Mise à jour des champs
                ctr.setNumeroCtr(updatedCtr.getNumeroCtr());
                ctr.setTypeOperation(updatedCtr.getTypeOperation());
                ctr.setStatutCtr(updatedCtr.getStatutCtr());
                ctr.setCodeValeur(updatedCtr.getCodeValeur());
                ctr.setMontant(updatedCtr.getMontant());
                ctr.setNombreElements(updatedCtr.getNombreElements());
                ctr.setNombreCarthago(updatedCtr.getNombreCarthago());
                ctr.setNombreFichiers(updatedCtr.getNombreFichiers());
                ctr.setMontantCarthago(updatedCtr.getMontantCarthago());
                ctr.setMontantFichiers(updatedCtr.getMontantFichiers());
                ctr.setEquilibre(updatedCtr.getEquilibre());
                ctr.setDifference(updatedCtr.getDifference());
                ctr.setGenereVersCtr(updatedCtr.getGenereVersCtr());
                ctr.setRecuParCtr(updatedCtr.getRecuParCtr());
                ctr.setDateGeneration(updatedCtr.getDateGeneration());
                ctr.setDateReception(updatedCtr.getDateReception());
                ctr.setRemiseDouble(updatedCtr.getRemiseDouble());
                ctr.setRemiseNonParvenue(updatedCtr.getRemiseNonParvenue());
                ctr.setChequeElectroniqueCtr(updatedCtr.getChequeElectroniqueCtr());
                ctr.setFichierEnvCtr(updatedCtr.getFichierEnvCtr());
                ctr.setSessionCtr(updatedCtr.getSessionCtr());
                ctr.setOperateurCtr(updatedCtr.getOperateurCtr());
                ctr.setDateTraitement(updatedCtr.getDateTraitement());
                CTR savedCtr = ctrRepository.save(ctr);
                return ResponseEntity.ok(savedCtr);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la mise à jour du CTR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Supprime un CTR par son ID
     * @param id ID du CTR à supprimer
     * @return Réponse vide si succès, erreur 404 si non trouvé
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCtr(@PathVariable Long id) {
        try {
            if (ctrRepository.existsById(id)) {
                ctrRepository.deleteById(id);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression du CTR: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Vérifie l'équilibrage d'un CTR
     * @param id ID du CTR à vérifier
     * @return Statut de l'équilibrage
     */
    @GetMapping("/{id}/equilibrage")
    public ResponseEntity<String> checkEquilibrage(@PathVariable Long id) {
        try {
            Optional<CTR> ctrOpt = ctrRepository.findById(id);
            if (ctrOpt.isPresent()) {
                CTR ctr = ctrOpt.get();
                boolean isEquilibre = ctr.getEquilibre() != null && ctr.getEquilibre();
                String message = isEquilibre ? "CTR équilibré" :
                        "CTR non équilibré (différence: " + ctr.getDifference() + ")";
                return ResponseEntity.ok(message);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification de l'équilibrage: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}