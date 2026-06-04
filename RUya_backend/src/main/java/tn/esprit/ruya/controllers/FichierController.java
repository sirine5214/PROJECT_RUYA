package tn.esprit.ruya.controllers;

import lombok.AllArgsConstructor;
import tn.esprit.ruya.services.FichierServ;
import tn.esprit.ruya.models.Dto;
import tn.esprit.ruya.models.Fichier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/api/fichiers")
@CrossOrigin(origins = "http://localhost:4200") // Permettre les requêtes cross-origin
public class FichierController {

    private final FichierServ fichierServ;

    // ✅ Get all fichiers
    @GetMapping
    public ResponseEntity<List<Fichier>> getAllFichiers() {
        try {
            List<Fichier> fichiers = fichierServ.getAllFichiers();
            return ResponseEntity.ok(fichiers);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des fichiers: " + e.getMessage());
            // Éviter de mettre le frontend en hors-ligne: renvoyer une liste vide
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
    }

    // ✅ Get fichier by ID
    @GetMapping("/{id}")
    public ResponseEntity<Fichier> getFichierById(@PathVariable Long id) {
        try {
            Optional<Fichier> fichier = fichierServ.getFichierById(id);
            return fichier.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération du fichier " + id + ": " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // ✅ Get all fichiers by user ID
    @GetMapping("/getallbyuser/{id}")
    public ResponseEntity<List<Fichier>> getAllFichierByUser(@PathVariable Long id) {
        try {
            List<Fichier> fichiers = fichierServ.getAllFichiersByUser(id);
            return ResponseEntity.ok(fichiers);
        } catch (Exception e) {
            System.err.println(
                    "❌ Erreur lors de la récupération des fichiers pour l'utilisateur " + id + " : " + e.getMessage());
            // Éviter de mettre le frontend en hors-ligne: renvoyer une liste vide
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
    }

    // ✅ Create new fichier
    @PostMapping
    public ResponseEntity<?> createFichier(@RequestBody Fichier fichier) {
        try {
            System.out.println("📝 DEBUG - Requête POST reçue pour créer fichier: " + fichier);

            // Validation des données requises
            if (fichier.getNomFichier() == null || fichier.getNomFichier().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le nom du fichier est requis."));
            }

            if (fichier.getUser() == null || fichier.getUser().getId() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "L'utilisateur est requis pour créer un fichier."));
            }

            // Validation supplémentaire des champs obligatoires
            if (fichier.getTypeFichier() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le type de fichier est requis."));
            }

            if (fichier.getSens() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le sens est requis."));
            }

            if (fichier.getMontant() == null || fichier.getMontant() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le montant doit être supérieur à 0."));
            }

            if (fichier.getNomber() == null || fichier.getNomber() <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Le nombre doit être supérieur à 0."));
            }

            Fichier created = fichierServ.createFichier(fichier);
            System.out.println("📝 DEBUG - Fichier créé avec succès: " + created.getNomFichier());
            return ResponseEntity
                    .ok(Map.of("success", true, "fichier", created, "message", "Fichier créé avec succès"));

        } catch (RuntimeException e) {
            System.err.println("❌ Erreur lors de la création du fichier: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue lors de la création du fichier: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("error", "Erreur lors de l'ajout du fichier"));
        }
    }

    // ✅ Update fichier by ID (ADMIN UNIQUEMENT)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFichier(@PathVariable Long id, @RequestBody Fichier updatedFichier) {
        try {
            // 🔒 VALIDATION ADMIN : Vérifier que l'utilisateur est admin
            if (updatedFichier.getUser() == null || updatedFichier.getUser().getId() == null) {
                return ResponseEntity.status(403)
                    .body(Map.of("error", "Utilisateur non spécifié"));
            }
            
            // Récupérer l'utilisateur depuis le service
            tn.esprit.ruya.models.User user = fichierServ.getUserById(updatedFichier.getUser().getId());
            if (user == null) {
                return ResponseEntity.status(403)
                    .body(Map.of("error", "Utilisateur non trouvé"));
            }
            
            // Vérifier si l'utilisateur est ADMIN
            if (user.getRole() != tn.esprit.ruya.models.RoleUser.ADMIN) {
                return ResponseEntity.status(403)
                    .body(Map.of(
                        "error", "Action non autorisée",
                        "message", "Seul un administrateur peut modifier un fichier"
                    ));
            }
            
            Fichier fichier = fichierServ.updateFichier(id, updatedFichier);
            return ResponseEntity.ok(fichier);
        } catch (RuntimeException e) {
            System.err.println("❌ Erreur lors de la mise à jour du fichier: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue lors de la mise à jour du fichier: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Erreur lors de la modification du fichier");
        }
    }

    // ✅ Delete fichier by ID (ADMIN UNIQUEMENT)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFichier(@PathVariable Long id, @RequestParam Long userId) {
        try {
            // 🔒 VALIDATION ADMIN : Vérifier que l'utilisateur est admin
            tn.esprit.ruya.models.User user = fichierServ.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(403)
                    .body(Map.of("error", "Utilisateur non trouvé"));
            }
            
            // Vérifier si l'utilisateur est ADMIN
            if (user.getRole() != tn.esprit.ruya.models.RoleUser.ADMIN) {
                return ResponseEntity.status(403)
                    .body(Map.of(
                        "error", "Action non autorisée",
                        "message", "Seul un administrateur peut supprimer un fichier"
                    ));
            }
            
            fichierServ.deleteFichier(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            System.err.println("❌ Erreur lors de la suppression du fichier: " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Erreur inattendue lors de la suppression du fichier: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Erreur lors de la suppression du fichier");
        }
    }

    // ✅ Get DTO avec statistiques générales
    @GetMapping("/dto")
    public ResponseEntity<Dto> getAllFichiersDto() {
        try {
            Dto dto = fichierServ.getAllFichiersDto();
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération du DTO: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🆕 Get statistiques par statut
    @GetMapping("/stats/status")
    public ResponseEntity<Map<String, Object>> getStatsByStatus() {
        try {
            Map<String, Object> stats = fichierServ.getStatsByStatus();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des statistiques par statut: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🆕 Get statistiques mensuelles
    @GetMapping("/stats/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyStats() {
        try {
            Map<String, Object> stats = fichierServ.getMonthlyStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des statistiques mensuelles: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🆕 Get fichiers avec filtres
    @GetMapping("/filter")
    public ResponseEntity<List<Fichier>> getFichiersWithFilters(
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            List<Fichier> fichiers = fichierServ.getFichiersWithFilters(
                    date, statut, type, search, page, size, sortBy, sortDir);
            return ResponseEntity.ok(fichiers);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du filtrage des fichiers: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🆕 Get montant total par type
    @GetMapping("/stats/amounts")
    public ResponseEntity<Map<String, Double>> getAmountsByType() {
        try {
            Map<String, Double> amounts = fichierServ.getAmountsByType();
            return ResponseEntity.ok(amounts);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des montants par type: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🆕 Get fichiers en attente
    @GetMapping("/pending")
    public ResponseEntity<List<Fichier>> getPendingFichiers() {
        try {
            List<Fichier> fichiers = fichierServ.getPendingFichiers();
            return ResponseEntity.ok(fichiers);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des fichiers en attente: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🆕 Get fichiers récents (7 derniers jours)
    @GetMapping("/recent")
    public ResponseEntity<List<Fichier>> getRecentFichiers() {
        try {
            List<Fichier> fichiers = fichierServ.getRecentFichiers();
            return ResponseEntity.ok(fichiers);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des fichiers récents: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🆕 Get statistiques par utilisateur
    @GetMapping("/stats/user/{userId}")
    public ResponseEntity<Map<String, Object>> getStatsByUser(@PathVariable Long userId) {
        try {
            Map<String, Object> stats = fichierServ.getStatsByUser(userId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des statistiques utilisateur: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🆕 Get alertes (fichiers rejetés récents)
    @GetMapping("/alerts")
    public ResponseEntity<List<Fichier>> getAlerts() {
        try {
            List<Fichier> alertes = fichierServ.getAlerts();
            return ResponseEntity.ok(alertes);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des alertes: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🆕 Get count total des fichiers
    @GetMapping("/count")
    public ResponseEntity<Long> getTotalCount() {
        try {
            List<Fichier> fichiers = fichierServ.getAllFichiers();
            return ResponseEntity.ok((long) fichiers.size());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du comptage des fichiers: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 🆕 Batch operations - Supprimer plusieurs fichiers (ADMIN UNIQUEMENT)
    @DeleteMapping("/batch")
    public ResponseEntity<?> deleteFichiers(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Long> ids = (List<Long>) request.get("ids");
            Long userId = ((Number) request.get("userId")).longValue();
            
            // 🔒 VALIDATION ADMIN
            tn.esprit.ruya.models.User user = fichierServ.getUserById(userId);
            if (user == null) {
                return ResponseEntity.status(403)
                    .body(Map.of("error", "Utilisateur non trouvé"));
            }
            
            if (user.getRole() != tn.esprit.ruya.models.RoleUser.ADMIN) {
                return ResponseEntity.status(403)
                    .body(Map.of(
                        "error", "Action non autorisée",
                        "message", "Seul un administrateur peut supprimer des fichiers"
                    ));
            }
            
            for (Long id : ids) {
                fichierServ.deleteFichier(id);
            }
            return ResponseEntity.ok(Map.of("message", "Fichiers supprimés avec succès"));
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la suppression en lot: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Erreur lors de la suppression en lot");
        }
    }

    // 🆕 Update status d'un fichier
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateFichierStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            Optional<Fichier> fichierOpt = fichierServ.getFichierById(id);
            if (fichierOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Fichier fichier = fichierOpt.get();
            fichier.setCodeValeur(status);

            Fichier updated = fichierServ.updateFichier(id, fichier);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour du statut: " + e.getMessage());
            return ResponseEntity.internalServerError().body("Erreur lors de la mise à jour du statut");
        }
    }
}