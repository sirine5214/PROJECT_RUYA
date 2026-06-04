package tn.esprit.ruya.services;

import lombok.AllArgsConstructor;
import tn.esprit.ruya.repositories.IFichierrepo;
import tn.esprit.ruya.models.Dto;
import tn.esprit.ruya.models.Fichier;
import tn.esprit.ruya.models.User;
import tn.esprit.ruya.repositories.IUserRepo;
import tn.esprit.ruya.services.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@AllArgsConstructor
@Service
public class FichierServ implements IFichierser {

    private IFichierrepo fichierRepo;
    private IUserRepo userRepository;
    private NotificationService notificationService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Fichier> getAllFichiers() {
        try {
            return fichierRepo.findAll();
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des fichiers: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<Fichier> getFichierById(Long id) {
        try {
            return fichierRepo.findById(id);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération du fichier " + id + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Fichier createFichier(Fichier fichier) {
        try {
            System.out.println("🔍 DEBUG - Création de fichier avec données: " + fichier);

            // Validation des données requises
            if (fichier.getNomFichier() == null || fichier.getNomFichier().trim().isEmpty()) {
                throw new RuntimeException("Le nom du fichier est requis.");
            }

            if (fichier.getUser() == null || fichier.getUser().getId() == null) {
                throw new RuntimeException("L'utilisateur est requis pour créer un fichier.");
            }

            // Récupération de l'utilisateur depuis la base de données
            User user = userRepository.findById(fichier.getUser().getId())
                    .orElseThrow(() -> new RuntimeException(
                            "Utilisateur non trouvé avec l'ID : " + fichier.getUser().getId()));

            // Vérifier si l'utilisateur est actif
            if (!user.getIsActive()) {
                throw new RuntimeException("L'utilisateur avec l'ID " + user.getId() + " n'est pas actif.");
            }

            System.out.println(
                    "🔍 DEBUG - Utilisateur trouvé et actif: " + user.getUsername() + " (ID: " + user.getId() + ")");
            fichier.setUser(user);

            // Initialisation des valeurs par défaut
            fichier.setCreatedAt(LocalDateTime.now());
            fichier.setUpdatedAt(LocalDateTime.now());

            // Validation des champs obligatoires
            if (fichier.getTypeFichier() == null) {
                throw new RuntimeException("Le type de fichier est requis.");
            }
            if (fichier.getSens() == null) {
                throw new RuntimeException("Le sens est requis.");
            }
            if (fichier.getMontant() == null || fichier.getMontant() <= 0) {
                throw new RuntimeException("Le montant doit être supérieur à 0.");
            }
            if (fichier.getNomber() == null || fichier.getNomber() <= 0) {
                throw new RuntimeException("Le nombre doit être supérieur à 0.");
            }

            // Set default values if not provided
            if (fichier.getSens() == null) {
                fichier.setSens(Fichier.Sens.SORTANT);
            }
            if (fichier.getMontant() == null) {
                fichier.setMontant(0.0);
            }
            if (fichier.getNomber() == null) {
                fichier.setNomber(0);
            }

            // Sauvegarde du fichier
            Fichier savedFichier = fichierRepo.save(fichier);
            System.out.println("🔍 DEBUG - Fichier sauvegardé avec succès: " + savedFichier.getNomFichier());

            // Créer automatiquement une notification pour l'ajout du fichier
            try {
                notificationService.creerNotificationAjout(savedFichier, user);
                System.out.println("🔍 DEBUG - Notification créée automatiquement pour le fichier: "
                        + savedFichier.getNomFichier());
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de la création de la notification: " + e.getMessage());
            }

            return savedFichier;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création du fichier: " + e.getMessage());
            throw new RuntimeException("Erreur lors de l'ajout du fichier: " + e.getMessage());
        }
    }

    @Override
    public Fichier updateFichier(Long id, Fichier updatedFichier) {
        try {
            return fichierRepo.findById(id).map(fichier -> {
                if (updatedFichier.getNomFichier() != null) {
                    fichier.setNomFichier(updatedFichier.getNomFichier());
                }
                if (updatedFichier.getNatureFichier() != null) {
                    fichier.setNatureFichier(updatedFichier.getNatureFichier());
                }
                if (updatedFichier.getCodeValeur() != null) {
                    fichier.setCodeValeur(updatedFichier.getCodeValeur());
                }
                if (updatedFichier.getTypeFichier() != null) {
                    fichier.setTypeFichier(updatedFichier.getTypeFichier());
                }
                if (updatedFichier.getMontant() != null) {
                    fichier.setMontant(updatedFichier.getMontant());
                }
                if (updatedFichier.getNomber() != null) {
                    fichier.setNomber(updatedFichier.getNomber());
                }
                if (updatedFichier.getSens() != null) {
                    fichier.setSens(updatedFichier.getSens());
                }
                return fichierRepo.save(fichier);
            }).orElseThrow(() -> new RuntimeException("Fichier non trouvé avec l'ID : " + id));
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la mise à jour du fichier: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la modification du fichier: " + e.getMessage());
        }
    }

    @Override
    public void deleteFichier(Long id) {
        try {
            if (!fichierRepo.existsById(id)) {
                throw new RuntimeException("Fichier non trouvé avec l'ID : " + id);
            }
            fichierRepo.deleteById(id);
            System.out.println("✅ Fichier supprimé avec succès: " + id);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la suppression du fichier: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la suppression du fichier: " + e.getMessage());
        }
    }

    @Override
    public Dto getAllFichiersDto() {
        try {
            Dto dto = new Dto();
            List<Fichier> fichiers = fichierRepo.findAll();

            dto.setFichierRemis(fichiers.stream()
                    .filter(f -> f.getSens() != null && f.getSens().name().equalsIgnoreCase("SORTANT")).count());
            dto.setFichierRecu(fichiers.stream()
                    .filter(f -> f.getSens() != null && f.getSens().name().equalsIgnoreCase("ENTRANT")).count());

            dto.setFichierRecucheque(
                    fichiers.stream().filter(f -> f.getSens() != null && f.getSens().name().equalsIgnoreCase("ENTRANT")
                            && f.getTypeFichier() == Fichier.TypeFichier.ELECTRONIQUE).count());
            dto.setFichierRecuprlv(
                    fichiers.stream().filter(f -> f.getSens() != null && f.getSens().name().equalsIgnoreCase("ENTRANT")
                            && f.getTypeFichier() == Fichier.TypeFichier.ELECTRONIQUE).count());
            dto.setFichierRecueffet(
                    fichiers.stream().filter(f -> f.getSens() != null && f.getSens().name().equalsIgnoreCase("ENTRANT")
                            && f.getTypeFichier() == Fichier.TypeFichier.MANUEL).count());
            dto.setFichierRecuvirment(
                    fichiers.stream().filter(f -> f.getSens() != null && f.getSens().name().equalsIgnoreCase("ENTRANT")
                            && f.getTypeFichier() == Fichier.TypeFichier.WEB).count());

            dto.setFichierRepris(10L);
            dto.setFichierRendu(10L);
            dto.setFichierRejeteffet(10L);
            dto.setFichierRejetprlv(10L);
            dto.setFichierRejetvirment(5L);
            dto.setFichierRejetcheque(6L);
            dto.setFichierRenducheque(15L);
            dto.setFichierRenduprlv(8L);
            dto.setFichierRendueffet(9L);
            dto.setFichierRenduvirment(3L);

            dto.setTotalMontant(
                    fichiers.stream().mapToDouble(f -> f.getMontant() != null ? f.getMontant() : 0.0).sum());
            dto.setTotalNomber(fichiers.stream().mapToInt(f -> f.getNomber() != null ? f.getNomber() : 0).sum());

            return dto;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des statistiques DTO: " + e.getMessage());
            return new Dto();
        }
    }

    // 🆕 Nouvelles méthodes pour le dashboard avancé

    /**
     * Statistiques par statut (REMIS, REJET, RENDU, EN_ATTENTE)
     */
    // Méthodes helper pour vérifier les codes (supporte format texte ET numérique)
    private boolean isCodeChequeRemis(String code) {
        if (code == null) return false;
        String upper = code.toUpperCase();
        return upper.equals("CHEQUE") || code.equals("30") || code.equals("31");
    }
    
    private boolean isCodeChequeRejet(String code) {
        if (code == null) return false;
        return code.equals("32") || code.equals("33");
    }
    
    private boolean isCodeChequeRendu(String code) {
        if (code == null) return false;
        return code.equals("34");
    }

    public Map<String, Object> getStatsByStatus() {
        try {
            List<Fichier> fichiers = fichierRepo.findAll();
            Map<String, Object> stats = new HashMap<>();

            // Calcul des statuts basés sur les codes (texte OU numérique)
            long remis = fichiers.stream()
                    .filter(f -> isCodeChequeRemis(f.getCodeValeur()))
                    .count();

            long rejet = fichiers.stream()
                    .filter(f -> isCodeChequeRejet(f.getCodeValeur()))
                    .count();

            long rendu = fichiers.stream()
                    .filter(f -> isCodeChequeRendu(f.getCodeValeur()))
                    .count();

            long enAttente = fichiers.stream()
                    .filter(f -> !isCodeChequeRemis(f.getCodeValeur()) &&
                                 !isCodeChequeRejet(f.getCodeValeur()) &&
                                 !isCodeChequeRendu(f.getCodeValeur()))
                    .count();

            stats.put("REMIS", remis);
            stats.put("REJET", rejet);
            stats.put("RENDU", rendu);
            stats.put("EN_ATTENTE", enAttente);
            stats.put("TOTAL", fichiers.size());

            return stats;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des statistiques par statut: " + e.getMessage());
            return new HashMap<>();
        }
    }

    // 🆕 Get statistiques mensuelles depuis la base de données
    public Map<String, Object> getMonthlyStats() {
        try {
            Map<String, Object> monthlyData = new HashMap<>();

            // Récupérer les données par mois depuis la base
            String sql = """
                        SELECT
                            TO_CHAR(CREATED_AT, 'MM') as mois,
                            COUNT(*) as nombre_fichiers,
                            SUM(MONTANT) as montant_total
                        FROM FICHIERS
                        WHERE CREATED_AT >= ADD_MONTHS(SYSDATE, -12)
                        GROUP BY TO_CHAR(CREATED_AT, 'MM')
                        ORDER BY mois
                    """;

            List<Object[]> results = entityManager.createNativeQuery(sql).getResultList();

            // Préparer les données pour le graphique
            List<String> labels = Arrays.asList("Jan", "Fév", "Mar", "Avr", "Mai", "Jun",
                    "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc");
            List<Integer> fichiers = new ArrayList<>();
            List<Double> montants = new ArrayList<>();

            // Initialiser avec des zéros
            for (int i = 0; i < 12; i++) {
                fichiers.add(0);
                montants.add(0.0);
            }

            // Remplir avec les vraies données
            for (Object[] row : results) {
                String mois = (String) row[0];
                Integer nombre = ((Number) row[1]).intValue();
                Double montant = ((Number) row[2]).doubleValue();

                int index = Integer.parseInt(mois) - 1; // Convertir MM en index (0-11)
                if (index >= 0 && index < 12) {
                    fichiers.set(index, nombre);
                    montants.set(index, montant / 1000); // Convertir en k DT
                }
            }

            monthlyData.put("labels", labels);
            monthlyData.put("fichiers", fichiers);
            monthlyData.put("montants", montants);

            System.out.println("📊 Données mensuelles récupérées depuis la base: " + monthlyData);
            return monthlyData;

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des statistiques mensuelles: " + e.getMessage());
            e.printStackTrace();
            return getDefaultMonthlyData(); // Données par défaut en cas d'erreur
        }
    }

    // Méthode de fallback avec données par défaut
    private Map<String, Object> getDefaultMonthlyData() {
        Map<String, Object> defaultData = new HashMap<>();
        List<String> labels = Arrays.asList("Jan", "Fév", "Mar", "Avr", "Mai", "Jun",
                "Jul", "Aoû", "Sep", "Oct", "Nov", "Déc");
        List<Integer> fichiers = Arrays.asList(30, 27, 30, 58, 54, 17, 56, 21, 30, 19, 30, 15);
        List<Double> montants = Arrays.asList(600.0, 500.0, 400.0, 1100.0, 850.0, 350.0,
                650.0, 600.0, 500.0, 250.0, 450.0, 750.0);

        defaultData.put("labels", labels);
        defaultData.put("fichiers", fichiers);
        defaultData.put("montants", montants);

        return defaultData;
    }

    /**
     * Fichiers avec filtres avancés et pagination
     */
    public List<Fichier> getFichiersWithFilters(String date, String statut, String type,
            String search, int page, int size,
            String sortBy, String sortDir) {
        try {
            List<Fichier> allFichiers = fichierRepo.findAll();
            List<Fichier> filteredFichiers = allFichiers.stream()
                    .filter(fichier -> {
                        // Filtre par date
                        if (date != null && !date.isEmpty()) {
                            if (fichier.getCreatedAt() == null ||
                                    !fichier.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                            .equals(date)) {
                                return false;
                            }
                        }

                        // Filtre par statut (basé sur codeValeur)
                        if (statut != null && !statut.isEmpty()) {
                            String codeValeur = fichier.getCodeValeur();
                            switch (statut) {
                                case "REMIS":
                                    if (!isCodeChequeRemis(codeValeur)) {
                                        return false;
                                    }
                                    break;
                                case "REJET":
                                    if (!isCodeChequeRejet(codeValeur)) {
                                        return false;
                                    }
                                    break;
                                case "RENDU":
                                    if (!isCodeChequeRendu(codeValeur)) {
                                        return false;
                                    }
                                    break;
                                case "EN_ATTENTE":
                                    if (isCodeChequeRemis(codeValeur) ||
                                        isCodeChequeRejet(codeValeur) ||
                                        isCodeChequeRendu(codeValeur)) {
                                        return false;
                                    }
                                    break;
                            }
                        }

                        // Filtre par type
                        if (type != null && !type.isEmpty()) {
                            if (fichier.getTypeFichier() == null
                                    || !type.equalsIgnoreCase(fichier.getTypeFichier().name())) {
                                return false;
                            }
                        }

                        // Filtre par recherche textuelle
                        if (search != null && !search.isEmpty()) {
                            String searchLower = search.toLowerCase();
                            if (!fichier.getNomFichier().toLowerCase().contains(searchLower)) {
                                return false;
                            }
                        }

                        return true;
                    })
                    .collect(Collectors.toList());

            // Tri
            if (sortBy != null && !sortBy.isEmpty()) {
                Comparator<Fichier> comparator = getComparator(sortBy);
                if ("desc".equalsIgnoreCase(sortDir)) {
                    comparator = comparator.reversed();
                }
                filteredFichiers.sort(comparator);
            }

            // Pagination
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, filteredFichiers.size());

            if (startIndex >= filteredFichiers.size()) {
                return new ArrayList<>();
            }

            return filteredFichiers.subList(startIndex, endIndex);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du filtrage des fichiers: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Montants totaux par type de fichier
     */
    public Map<String, Double> getAmountsByType() {
        try {
            List<Fichier> fichiers = fichierRepo.findAll();

            return fichiers.stream()
                    .filter(f -> f.getMontant() != null && f.getTypeFichier() != null)
                    .collect(Collectors.groupingBy(
                            f -> f.getTypeFichier().name(),
                            Collectors.summingDouble(Fichier::getMontant)));
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des montants par type: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Fichiers en attente (sans codeValeur ou avec codeValeur non standard)
     */
    public List<Fichier> getPendingFichiers() {
        try {
            return fichierRepo.findAll().stream()
                    .filter(f -> !isCodeChequeRemis(f.getCodeValeur()) &&
                                 !isCodeChequeRejet(f.getCodeValeur()) &&
                                 !isCodeChequeRendu(f.getCodeValeur()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des fichiers en attente: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Fichiers récents (7 derniers jours)
     */
    public List<Fichier> getRecentFichiers() {
        try {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            return fichierRepo.findAll().stream()
                    .filter(f -> f.getCreatedAt() != null && f.getCreatedAt().isAfter(sevenDaysAgo))
                    .sorted(Comparator.comparing(Fichier::getCreatedAt).reversed())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des fichiers récents: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Statistiques par utilisateur
     */
    public Map<String, Object> getStatsByUser(Long userId) {
        try {
            List<Fichier> userFichiers = fichierRepo.findAll().stream()
                    .filter(f -> f.getUser() != null && f.getUser().getId().equals(userId))
                    .collect(Collectors.toList());

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalFichiers", userFichiers.size());
            stats.put("totalMontant", userFichiers.stream()
                    .mapToDouble(f -> f.getMontant() != null ? f.getMontant() : 0.0)
                    .sum());
            stats.put("fichiersParType", userFichiers.stream()
                    .collect(Collectors.groupingBy(Fichier::getTypeFichier, Collectors.counting())));

            return stats;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des statistiques utilisateur: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Alertes (fichiers rejetés récents)
     */
    public List<Fichier> getAlerts() {
        try {
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            return fichierRepo.findAll().stream()
                    .filter(f -> f.getCodeValeur() != null &&
                            (f.getCodeValeur().equals("32") || f.getCodeValeur().equals("33")))
                    .filter(f -> f.getCreatedAt() != null && f.getCreatedAt().isAfter(thirtyDaysAgo))
                    .sorted(Comparator.comparing(Fichier::getCreatedAt).reversed())
                    .limit(10) // Limiter à 10 alertes
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des alertes: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // Méthodes utilitaires privées

    private String getMoisLabel(String moisKey) {
        Map<String, String> moisMapping = new HashMap<>();
        moisMapping.put("01", "Jan");
        moisMapping.put("02", "Fév");
        moisMapping.put("03", "Mar");
        moisMapping.put("04", "Avr");
        moisMapping.put("05", "Mai");
        moisMapping.put("06", "Jun");
        moisMapping.put("07", "Jul");
        moisMapping.put("08", "Aoû");
        moisMapping.put("09", "Sep");
        moisMapping.put("10", "Oct");
        moisMapping.put("11", "Nov");
        moisMapping.put("12", "Déc");

        if (moisKey.contains("-")) {
            String mois = moisKey.split("-")[1];
            return moisMapping.get(mois);
        }
        return null;
    }

    private Comparator<Fichier> getComparator(String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "nomfichier":
                return Comparator.comparing(Fichier::getNomFichier);
            case "typefichier":
                return Comparator.comparing(Fichier::getTypeFichier);
            case "montant":
                return Comparator.comparing(f -> f.getMontant() != null ? f.getMontant() : 0.0);
            case "createdat":
                return Comparator.comparing(Fichier::getCreatedAt);
            default:
                return Comparator.comparing(Fichier::getCreatedAt);
        }
    }

    public List<Fichier> getAllFichiersByUser(Long id) {
        try {
            return fichierRepo.findByUserId(id);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des fichiers par utilisateur: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 🔐 Récupérer un utilisateur par ID (pour validation admin)
     */
    public User getUserById(Long userId) {
        try {
            return userRepository.findById(userId).orElse(null);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération de l'utilisateur: " + e.getMessage());
            return null;
        }
    }
}