// DashboardController.java - Version corrigée pour correspondre à l'image
package tn.esprit.ruya.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ruya.models.DashboardResponseDTO;
import tn.esprit.ruya.services.DashboardService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = {"http://localhost:4200", "http://127.0.0.1:4200"}, allowCredentials = "true")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Endpoint principal pour récupérer les données du dashboard
     * GET /api/dashboard/data
     */
    @GetMapping("/data")
    public ResponseEntity<DashboardResponseDTO> getDashboardData() {
        try {
            DashboardResponseDTO response = dashboardService.getDashboardDataCorrected();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // En cas d'erreur, retourner des données d'erreur
            DashboardResponseDTO errorResponse = new DashboardResponseDTO();
            errorResponse.setConnected(false);
            errorResponse.setLastUpdate(LocalDateTime.now().toString());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Endpoint pour rafraîchir les données
     * POST /api/dashboard/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<DashboardResponseDTO> refreshDashboardData() {
        try {
            // Simuler un délai de traitement
            Thread.sleep(1000);

            DashboardResponseDTO response = dashboardService.getDashboardDataCorrected();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            DashboardResponseDTO errorResponse = new DashboardResponseDTO();
            errorResponse.setConnected(false);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Endpoint pour récupérer les données d'une période spécifique
     * GET /api/dashboard/data/period?startDate=2025-08-28&endDate=2025-08-28
     */
    @GetMapping("/data/period")
    public ResponseEntity<DashboardResponseDTO> getDashboardDataForPeriod(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");

            DashboardResponseDTO response = dashboardService.getDashboardDataForPeriodCorrected(start, end);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            DashboardResponseDTO errorResponse = new DashboardResponseDTO();
            errorResponse.setConnected(false);
            return ResponseEntity.status(400).body(errorResponse);
        }
    }

    /**
     * Health check endpoint
     * GET /api/dashboard/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Dashboard Service is running - Session: 28/08/2025");
    }

    /**
     * Endpoint de test pour vérifier la connectivité
     * GET /api/dashboard/test
     */
    @GetMapping("/test")
    public ResponseEntity<String> testConnection() {
        return ResponseEntity.ok("Connection successful - RUYA Dashboard Backend");
    }
}