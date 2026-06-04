package tn.esprit.ruya.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.ruya.models.ChatRequest;
import tn.esprit.ruya.models.ChatResponse;
import tn.esprit.ruya.services.FichierServ;
import tn.esprit.ruya.models.Fichier;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Arrays;

@Service
@AllArgsConstructor
public class ChatbotService {

    private FichierServ fichierServ;

    public ChatResponse processQuestion(ChatRequest chatRequest) {
        String question = chatRequest.getQuestion();
        String lowerQuestion = question.toLowerCase();

        try {
            // Questions sur les statistiques générales
            if (lowerQuestion.contains("statistique") || lowerQuestion.contains("résumé")
                    || lowerQuestion.contains("général")) {
                return getGeneralStatistics();
            }

            // Questions spécifiques
            if (question.contains("fichiers") && (question.contains("aujourd'hui") || question.contains("ajoutés"))) {
                return getTodayFiles();
            }

            if (question.contains("montant") && question.contains("aujourd'hui")) {
                return getTodayAmount();
            }

            if (question.contains("montant") && question.contains("mois")) {
                return getMonthAmount();
            }

            // Questions sur les temps de traitement
            if (lowerQuestion.contains("temps") || lowerQuestion.contains("traitement")) {
                if (chatRequest.getUserId() != null) {
                    try {
                        Long userId = Long.parseLong(chatRequest.getUserId());
                        return getProcessingTimesByUser(lowerQuestion, userId);
                    } catch (NumberFormatException e) {
                        return new ChatResponse("Erreur: ID utilisateur invalide.", "error", null);
                    }
                } else {
                    return getProcessingTimes(lowerQuestion);
                }
            }

            // Questions sur l'adresse Attijary
            if (lowerQuestion.contains("adresse") || lowerQuestion.contains("siège")
                    || lowerQuestion.contains("attijary")) {
                return getAttijaryAddress();
            }

            // Questions sur les montants
            if (lowerQuestion.contains("montant") || lowerQuestion.contains("total")) {
                return getTotalAmounts();
            }

            // Questions sur les fichiers rejetés
            if (lowerQuestion.contains("rejeté") || lowerQuestion.contains("rejet")) {
                return getRejectedFiles();
            }

            // Questions sur les fichiers remis
            if (lowerQuestion.contains("remis") || lowerQuestion.contains("accepté")) {
                return getAcceptedFiles();
            }

            // Questions sur les utilisateurs
            if (lowerQuestion.contains("utilisateur") || lowerQuestion.contains("qui")) {
                return getUserStatistics();
            }

            // Question par défaut
            return getDefaultResponse();

        } catch (Exception e) {
            return new ChatResponse(
                    "Une erreur s'est produite lors du traitement de votre question.",
                    "error",
                    null);
        }
    }

    private ChatResponse getGeneralStatistics() {
        try {
            List<Fichier> allFiles = fichierServ.getAllFichiers();

            Map<String, Long> statusCount = allFiles.stream()
                    .collect(Collectors.groupingBy(
                            f -> f.getSens() != null ? f.getSens().name() : "INCONNU",
                            Collectors.counting()));

            double totalAmount = allFiles.stream()
                    .mapToDouble(f -> f.getMontant() != null ? f.getMontant() : 0.0)
                    .sum();

            StringBuilder response = new StringBuilder();
            response.append("📊 **Statistiques Générales RU'ya**\n\n");
            response.append("📁 Total des fichiers: ").append(allFiles.size()).append("\n");
            response.append("💰 Montant total: ").append(String.format("%.2f", totalAmount)).append(" DT\n\n");

            response.append("**Répartition par statut:**\n");
            statusCount.forEach((status, count) -> response.append("• ").append(status).append(": ").append(count)
                    .append(" fichiers\n"));

            return new ChatResponse(response.toString(), "success", statusCount);

        } catch (Exception e) {
            return new ChatResponse("Erreur lors de la récupération des statistiques.", "error", null);
        }
    }

    private ChatResponse getTodayFiles() {
        try {
            List<Fichier> allFiles = fichierServ.getAllFichiers();
            LocalDate today = LocalDate.now();

            // Filtrer les fichiers créés aujourd'hui
            List<Fichier> todayFiles = allFiles.stream()
                    .filter(f -> f.getCreatedAt() != null &&
                            f.getCreatedAt().toLocalDate().equals(today))
                    .collect(Collectors.toList());

            // Calculer les statistiques par type
            Map<String, Long> filesByType = todayFiles.stream()
                    .collect(Collectors.groupingBy(
                            f -> f.getTypeFichier() != null ? f.getTypeFichier().name() : "INCONNU",
                            Collectors.counting()));

            double todayAmount = todayFiles.stream()
                    .mapToDouble(f -> f.getMontant() != null ? f.getMontant() : 0.0)
                    .sum();

            StringBuilder response = new StringBuilder();
            response.append(String.format(
                    "📅 **Fichiers Ajoutés Aujourd'hui (%s)**\n\n",
                    today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));

            response.append("📊 **Résumé:**\n");
            response.append("📁 Total fichiers ajoutés: ").append(todayFiles.size()).append("\n");
            response.append("💰 Montant total: ").append(String.format("%.2f", todayAmount)).append(" DT\n\n");

            if (!filesByType.isEmpty()) {
                response.append("📋 **Répartition par type:**\n");
                filesByType.forEach((type, count) -> response.append("• ").append(type).append(": ").append(count)
                        .append(" fichiers\n"));
            } else {
                response.append("ℹ️ Aucun fichier ajouté aujourd'hui.\n");
            }

            response.append("\n✅ Données mises à jour en temps réel depuis votre base Oracle!");

            return new ChatResponse(response.toString(), "success", todayFiles.size());

        } catch (Exception e) {
            return new ChatResponse("Erreur lors de la récupération des fichiers du jour.", "error", null);
        }
    }

    private ChatResponse getTodayAmount() {
        try {
            List<Fichier> allFiles = fichierServ.getAllFichiers();
            LocalDate today = LocalDate.now();

            // Filtrer les fichiers créés aujourd'hui
            List<Fichier> todayFiles = allFiles.stream()
                    .filter(f -> f.getCreatedAt() != null &&
                            f.getCreatedAt().toLocalDate().equals(today))
                    .collect(Collectors.toList());

            double todayAmount = todayFiles.stream()
                    .mapToDouble(f -> f.getMontant() != null ? f.getMontant() : 0.0)
                    .sum();

            // Calculer par type
            Map<String, Double> amountsByType = todayFiles.stream()
                    .collect(Collectors.groupingBy(
                            f -> f.getTypeFichier() != null ? f.getTypeFichier().name() : "INCONNU",
                            Collectors.summingDouble(f -> f.getMontant() != null ? f.getMontant() : 0.0)));

            StringBuilder response = new StringBuilder();
            response.append(String.format(
                    "💰 **Montant Total Aujourd'hui (%s)**\n\n",
                    today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))));

            response.append("💵 **Montant Global: ").append(String.format("%.2f DT", todayAmount)).append("**\n\n");

            if (!amountsByType.isEmpty()) {
                response.append("📊 **Répartition par type:**\n");
                amountsByType.forEach((type, amount) -> response.append("• ").append(type).append(": ")
                        .append(String.format("%.2f DT", amount)).append("\n"));
            } else {
                response.append("ℹ️ Aucun montant enregistré aujourd'hui.\n");
            }

            response.append("\n📈 Basé sur ").append(todayFiles.size()).append(" fichiers traités aujourd'hui");

            return new ChatResponse(response.toString(), "success", todayAmount);

        } catch (Exception e) {
            return new ChatResponse("Erreur lors du calcul du montant du jour.", "error", null);
        }
    }

    private ChatResponse getMonthAmount() {
        try {
            List<Fichier> allFiles = fichierServ.getAllFichiers();
            LocalDate now = LocalDate.now();
            LocalDate startOfMonth = now.withDayOfMonth(1);

            // Filtrer les fichiers de ce mois
            List<Fichier> monthFiles = allFiles.stream()
                    .filter(f -> f.getCreatedAt() != null &&
                            !f.getCreatedAt().toLocalDate().isBefore(startOfMonth) &&
                            !f.getCreatedAt().toLocalDate().isAfter(now))
                    .collect(Collectors.toList());

            double monthAmount = monthFiles.stream()
                    .mapToDouble(f -> f.getMontant() != null ? f.getMontant() : 0.0)
                    .sum();

            // Calculer par type
            Map<String, Double> amountsByType = monthFiles.stream()
                    .collect(Collectors.groupingBy(
                            f -> f.getTypeFichier() != null ? f.getTypeFichier().name() : "INCONNU",
                            Collectors.summingDouble(f -> f.getMontant() != null ? f.getMontant() : 0.0)));

            StringBuilder response = new StringBuilder();
            response.append(String.format(
                    "📅 **Montant Total Ce Mois (%s %d)**\n\n",
                    now.getMonth().toString(),
                    now.getYear()));

            response.append("💰 **Montant Global: ").append(String.format("%.2f DT", monthAmount)).append("**\n\n");

            if (!amountsByType.isEmpty()) {
                response.append("📊 **Répartition par type:**\n");
                amountsByType.forEach((type, amount) -> response.append("• ").append(type).append(": ")
                        .append(String.format("%.2f DT", amount)).append("\n"));
            } else {
                response.append("ℹ️ Aucun montant enregistré ce mois.\n");
            }

            response.append("\n📈 Basé sur ").append(monthFiles.size()).append(" fichiers traités ce mois");

            return new ChatResponse(response.toString(), "success", monthAmount);

        } catch (Exception e) {
            return new ChatResponse("Erreur lors du calcul du montant du mois.", "error", null);
        }
    }

    private ChatResponse getTotalAmounts() {
        try {
            List<Fichier> allFiles = fichierServ.getAllFichiers();

            double totalAmount = allFiles.stream()
                    .mapToDouble(f -> f.getMontant() != null ? f.getMontant() : 0.0)
                    .sum();

            Map<String, Double> amountByStatus = allFiles.stream()
                    .collect(Collectors.groupingBy(
                            f -> f.getSens() != null ? f.getSens().name() : "INCONNU",
                            Collectors.summingDouble(f -> f.getMontant() != null ? f.getMontant() : 0.0)));

            StringBuilder response = new StringBuilder();
            response.append("💰 **Analyse des Montants**\n\n");
            response.append("💵 Montant total global: ").append(String.format("%.2f", totalAmount)).append(" DT\n\n");

            response.append("**Répartition par statut:**\n");
            amountByStatus.forEach((status, amount) -> response.append("• ").append(status).append(": ")
                    .append(String.format("%.2f", amount)).append(" DT\n"));

            return new ChatResponse(response.toString(), "success", amountByStatus);

        } catch (Exception e) {
            return new ChatResponse("Erreur lors du calcul des montants.", "error", null);
        }
    }

    private ChatResponse getRejectedFiles() {
        try {
            List<Fichier> allFiles = fichierServ.getAllFichiers();

            List<Fichier> rejectedFiles = allFiles.stream()
                    .filter(f -> f.getSens() != null && f.getSens().name().equalsIgnoreCase("REJET"))
                    .collect(Collectors.toList());

            String response = String.format(
                    "❌ **Fichiers Rejetés**\n\n" +
                            "📁 Nombre de fichiers rejetés: %d\n" +
                            "📊 Pourcentage: %.1f%% du total\n\n" +
                            "💡 Consultez le tableau de bord pour plus de détails sur les raisons de rejet.",
                    rejectedFiles.size(),
                    allFiles.size() > 0 ? (rejectedFiles.size() * 100.0 / allFiles.size()) : 0);

            return new ChatResponse(response, "info", rejectedFiles.size());

        } catch (Exception e) {
            return new ChatResponse("Erreur lors de la récupération des fichiers rejetés.", "error", null);
        }
    }

    private ChatResponse getAcceptedFiles() {
        try {
            List<Fichier> allFiles = fichierServ.getAllFichiers();

            List<Fichier> acceptedFiles = allFiles.stream()
                    .filter(f -> f.getSens() != null && f.getSens().name().equalsIgnoreCase("REMIS"))
                    .collect(Collectors.toList());

            double acceptedAmount = acceptedFiles.stream()
                    .mapToDouble(f -> f.getMontant() != null ? f.getMontant() : 0.0)
                    .sum();

            String response = String.format(
                    "✅ **Fichiers Remis (Acceptés)**\n\n" +
                            "📁 Nombre de fichiers remis: %d\n" +
                            "💰 Montant total remis: %.2f DT\n" +
                            "📊 Pourcentage: %.1f%% du total\n\n" +
                            "🎉 Excellent travail d'équipe!",
                    acceptedFiles.size(),
                    acceptedAmount,
                    allFiles.size() > 0 ? (acceptedFiles.size() * 100.0 / allFiles.size()) : 0);

            return new ChatResponse(response, "success", acceptedFiles.size());

        } catch (Exception e) {
            return new ChatResponse("Erreur lors de la récupération des fichiers acceptés.", "error", null);
        }
    }

    private ChatResponse getUserStatistics() {
        try {
            List<Fichier> allFiles = fichierServ.getAllFichiers();

            Map<Long, Long> filesByUser = allFiles.stream()
                    .filter(f -> f.getUser() != null)
                    .collect(Collectors.groupingBy(
                            f -> f.getUser().getId(),
                            Collectors.counting()));

            StringBuilder response = new StringBuilder();
            response.append("👥 **Statistiques Utilisateurs**\n\n");
            response.append("👤 Nombre d'utilisateurs actifs: ").append(filesByUser.size()).append("\n\n");

            response.append("**Top utilisateurs:**\n");
            filesByUser.entrySet().stream()
                    .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                    .limit(5)
                    .forEach(entry -> response.append("• Utilisateur ID ").append(entry.getKey())
                            .append(": ").append(entry.getValue()).append(" fichiers\n"));

            return new ChatResponse(response.toString(), "success", filesByUser);

        } catch (Exception e) {
            return new ChatResponse("Erreur lors de la récupération des statistiques utilisateurs.", "error", null);
        }
    }

    private ChatResponse getProcessingTimes(String question) {
        try {
            List<Fichier> allFiles = fichierServ.getAllFichiers();

            if (question.contains("chèque")) {
                return getProcessingTimesByType("CHEQUE", allFiles);
            } else if (question.contains("prélèvement")) {
                return getProcessingTimesByType("PRELEVEMENT", allFiles);
            } else if (question.contains("virement")) {
                return getProcessingTimesByType("VIREMENT", allFiles);
            } else if (question.contains("effet")) {
                return getProcessingTimesByType("EFFET", allFiles);
            } else {
                // Vue générale de tous les types
                return getAllProcessingTimes(allFiles);
            }

        } catch (Exception e) {
            return new ChatResponse("Erreur lors de la récupération des temps de traitement.", "error", null);
        }
    }

    // Méthode pour obtenir les temps de traitement par utilisateur
    public ChatResponse getProcessingTimesByUser(String question, Long userId) {
        try {
            List<Fichier> allFiles = fichierServ.getAllFichiers();

            // Filtrer par utilisateur
            List<Fichier> userFiles = allFiles.stream()
                    .filter(f -> f.getUser() != null && f.getUser().getId().equals(userId))
                    .collect(Collectors.toList());

            if (question.contains("chèque")) {
                return getProcessingTimesByTypeAndUser("CHEQUE", userFiles, userId);
            } else if (question.contains("prélèvement")) {
                return getProcessingTimesByTypeAndUser("PRELEVEMENT", userFiles, userId);
            } else if (question.contains("virement")) {
                return getProcessingTimesByTypeAndUser("VIREMENT", userFiles, userId);
            } else if (question.contains("effet")) {
                return getProcessingTimesByTypeAndUser("EFFET", userFiles, userId);
            } else {
                // Vue générale pour cet utilisateur
                return getAllProcessingTimesByUser(userFiles, userId);
            }

        } catch (Exception e) {
            return new ChatResponse("Erreur lors de la récupération des temps de traitement pour l'utilisateur.",
                    "error", null);
        }
    }

    private ChatResponse getProcessingTimesByTypeAndUser(String type, List<Fichier> userFiles, Long userId) {
        try {
            // Filtrer les fichiers par type pour cet utilisateur
            List<Fichier> typeFiles = userFiles.stream()
                    .filter(f -> f.getTypeFichier() != null &&
                            f.getTypeFichier().name().toUpperCase().contains(type))
                    .collect(Collectors.toList());

            if (typeFiles.isEmpty()) {
                return new ChatResponse(
                        String.format("ℹ️ Aucun fichier de type %s trouvé pour votre compte.", type),
                        "info", 0L);
            }

            // Calculer les statistiques de traitement
            long totalFiles = typeFiles.size();

            // Calculer les fichiers traités dans les derniers jours
            LocalDate now = LocalDate.now();

            // Fichiers traités aujourd'hui
            long todayCount = typeFiles.stream()
                    .filter(f -> f.getCreatedAt() != null &&
                            f.getCreatedAt().toLocalDate().equals(now))
                    .count();

            // Fichiers traités cette semaine
            long weekCount = typeFiles.stream()
                    .filter(f -> f.getCreatedAt() != null &&
                            f.getCreatedAt().toLocalDate().isAfter(now.minusDays(7)))
                    .count();

            // Fichiers traités ce mois
            long monthCount = typeFiles.stream()
                    .filter(f -> f.getCreatedAt() != null &&
                            f.getCreatedAt().toLocalDate().isAfter(now.minusDays(30)))
                    .count();

            // **CALCUL DES VRAIS TEMPS DE TRAITEMENT POUR CET UTILISATEUR**
            List<Fichier> processedFiles = typeFiles.stream()
                    .filter(f -> f.getCreatedAt() != null && f.getUpdatedAt() != null)
                    .collect(Collectors.toList());

            // Calculer les temps de traitement réels en heures
            List<Double> processingTimesHours = processedFiles.stream()
                    .map(f -> {
                        long seconds = java.time.Duration.between(f.getCreatedAt(), f.getUpdatedAt()).getSeconds();
                        return seconds / 3600.0; // Convertir en heures
                    })
                    .collect(Collectors.toList());

            // Statistiques réelles
            double avgHours = processingTimesHours.isEmpty() ? 0
                    : processingTimesHours.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double minHours = processingTimesHours.isEmpty() ? 0
                    : processingTimesHours.stream().mapToDouble(Double::doubleValue).min().orElse(0);
            double maxHours = processingTimesHours.isEmpty() ? 0
                    : processingTimesHours.stream().mapToDouble(Double::doubleValue).max().orElse(0);

            StringBuilder response = new StringBuilder();
            String emoji = getTypeEmoji(type);

            response.append(String.format("%s **Vos Temps de Traitement - %s**\n\n", emoji, type));

            response.append("👤 **Vos Statistiques Personnelles:**\n");
            response.append("• Vos fichiers total: ").append(totalFiles).append("\n");
            response.append("• Fichiers avec temps calculé: ").append(processedFiles.size()).append("\n");
            response.append("• Traités aujourd'hui: ").append(todayCount).append("\n");
            response.append("• Traités cette semaine: ").append(weekCount).append("\n");
            response.append("• Traités ce mois: ").append(monthCount).append("\n\n");

            if (!processingTimesHours.isEmpty()) {
                response.append("⏱️ **Vos Temps de Traitement Réels:**\n");
                response.append("• Temps moyen: ").append(formatProcessingTime(avgHours)).append("\n");
                response.append("• Temps minimum: ").append(formatProcessingTime(minHours)).append("\n");
                response.append("• Temps maximum: ").append(formatProcessingTime(maxHours)).append("\n\n");

                // Répartition par tranches de temps
                response.append("📈 **Répartition de Vos Délais:**\n");
                response.append(getProcessingTimeDistribution(processingTimesHours));
            } else {
                response.append("ℹ️ Aucune donnée de traitement disponible pour vos fichiers de ce type.\n\n");
            }

            // Ajouter des détails spécifiques par type
            response.append(getTypeSpecificInfo(type));

            response.append(String.format("\n🔑 *Calculé uniquement depuis vos fichiers (User ID: %d)*", userId));

            return new ChatResponse(response.toString(), "info", totalFiles);

        } catch (Exception e) {
            return new ChatResponse("Erreur lors du calcul de vos temps de traitement: " + e.getMessage(), "error",
                    null);
        }
    }

    private ChatResponse getAllProcessingTimesByUser(List<Fichier> userFiles, Long userId) {
        try {
            if (userFiles.isEmpty()) {
                return new ChatResponse(
                        "ℹ️ Aucun fichier trouvé pour votre compte.",
                        "info", 0L);
            }

            Map<String, Long> typeStats = userFiles.stream()
                    .filter(f -> f.getTypeFichier() != null)
                    .collect(Collectors.groupingBy(
                            f -> f.getTypeFichier().name().toUpperCase(),
                            Collectors.counting()));

            StringBuilder response = new StringBuilder();
            response.append("⏱️ **Vos Temps de Traitement - Vue Générale**\n\n");

            response.append("👤 **Vos Types de Fichiers:**\n");

            // Afficher les 4 types principaux
            String[] mainTypes = { "CHEQUE", "PRELEVEMENT", "EFFET", "VIREMENT" };

            for (String type : mainTypes) {
                long count = typeStats.getOrDefault(type, 0L);
                String emoji = getTypeEmoji(type);

                if (count > 0) {
                    // Calculer le temps moyen pour ce type
                    List<Fichier> typeFiles = userFiles.stream()
                            .filter(f -> f.getTypeFichier() != null &&
                                    f.getTypeFichier().name().toUpperCase().contains(type))
                            .filter(f -> f.getCreatedAt() != null && f.getUpdatedAt() != null)
                            .collect(Collectors.toList());

                    if (!typeFiles.isEmpty()) {
                        double avgHours = typeFiles.stream()
                                .mapToDouble(f -> {
                                    long seconds = java.time.Duration.between(f.getCreatedAt(), f.getUpdatedAt())
                                            .getSeconds();
                                    return seconds / 3600.0;
                                })
                                .average().orElse(0);

                        response.append(String.format("%s **%s**: %d fichiers (Temps moyen: %s)\n",
                                emoji, type, count, formatProcessingTime(avgHours)));
                    } else {
                        response.append(String.format("%s **%s**: %d fichiers (Pas de données de traitement)\n",
                                emoji, type, count));
                    }
                } else {
                    response.append(String.format("%s **%s**: Aucun fichier\n", emoji, type));
                }
            }

            // Afficher les autres types s'il y en a
            typeStats.entrySet().stream()
                    .filter(entry -> !Arrays.asList(mainTypes).contains(entry.getKey()))
                    .forEach(entry -> {
                        String emoji = getTypeEmoji(entry.getKey());
                        response.append(String.format("%s **%s**: %d fichiers\n",
                                emoji, entry.getKey(), entry.getValue()));
                    });

            response.append("\n📈 **Total de Vos Fichiers:** ").append(userFiles.size()).append("\n");
            response.append(String.format("\n🔑 *Analyse personnalisée pour votre compte (User ID: %d)*", userId));

            return new ChatResponse(response.toString(), "info", (long) userFiles.size());

        } catch (Exception e) {
            return new ChatResponse("Erreur lors du calcul général de vos temps: " + e.getMessage(), "error", null);
        }
    }

    private ChatResponse getProcessingTimesByType(String type, List<Fichier> allFiles) {
        try {
            // Filtrer les fichiers par type
            List<Fichier> typeFiles = allFiles.stream()
                    .filter(f -> f.getTypeFichier() != null &&
                            f.getTypeFichier().name().toUpperCase().contains(type))
                    .collect(Collectors.toList());

            // Calculer les statistiques de traitement
            long totalFiles = typeFiles.size();

            // Calculer les fichiers traités dans les derniers jours
            LocalDate now = LocalDate.now();

            // Fichiers traités aujourd'hui
            long todayCount = typeFiles.stream()
                    .filter(f -> f.getCreatedAt() != null &&
                            f.getCreatedAt().toLocalDate().equals(now))
                    .count();

            // Fichiers traités cette semaine
            long weekCount = typeFiles.stream()
                    .filter(f -> f.getCreatedAt() != null &&
                            f.getCreatedAt().toLocalDate().isAfter(now.minusDays(7)))
                    .count();

            // Fichiers traités ce mois
            long monthCount = typeFiles.stream()
                    .filter(f -> f.getCreatedAt() != null &&
                            f.getCreatedAt().toLocalDate().isAfter(now.minusDays(30)))
                    .count();

            // **CALCUL DES VRAIS TEMPS DE TRAITEMENT**
            List<Fichier> processedFiles = typeFiles.stream()
                    .filter(f -> f.getCreatedAt() != null && f.getUpdatedAt() != null)
                    .collect(Collectors.toList());

            // Calculer les temps de traitement réels en heures
            List<Double> processingTimesHours = processedFiles.stream()
                    .map(f -> {
                        long seconds = java.time.Duration.between(f.getCreatedAt(), f.getUpdatedAt()).getSeconds();
                        return seconds / 3600.0; // Convertir en heures
                    })
                    .collect(Collectors.toList());

            // Statistiques réelles
            double avgHours = processingTimesHours.isEmpty() ? 0
                    : processingTimesHours.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double minHours = processingTimesHours.isEmpty() ? 0
                    : processingTimesHours.stream().mapToDouble(Double::doubleValue).min().orElse(0);
            double maxHours = processingTimesHours.isEmpty() ? 0
                    : processingTimesHours.stream().mapToDouble(Double::doubleValue).max().orElse(0);

            StringBuilder response = new StringBuilder();
            String emoji = getTypeEmoji(type);

            response.append(String.format("%s **Temps de Traitement Réels - %s**\n\n", emoji, type));

            response.append("📊 **Statistiques de la Base:**\n");
            response.append("• Total fichiers: ").append(totalFiles).append("\n");
            response.append("• Fichiers avec temps calculé: ").append(processedFiles.size()).append("\n");
            response.append("• Traités aujourd'hui: ").append(todayCount).append("\n");
            response.append("• Traités cette semaine: ").append(weekCount).append("\n");
            response.append("• Traités ce mois: ").append(monthCount).append("\n\n");

            if (!processingTimesHours.isEmpty()) {
                response.append("⏱️ **Temps de Traitement Réels:**\n");
                response.append("• Temps moyen: ").append(formatProcessingTime(avgHours)).append("\n");
                response.append("• Temps minimum: ").append(formatProcessingTime(minHours)).append("\n");
                response.append("• Temps maximum: ").append(formatProcessingTime(maxHours)).append("\n\n");

                // Répartition par tranches de temps
                response.append("📈 **Répartition des Délais:**\n");
                response.append(getProcessingTimeDistribution(processingTimesHours));
            } else {
                response.append("ℹ️ Aucune donnée de traitement disponible pour ce type.\n\n");
            }

            // Ajouter des détails spécifiques par type
            response.append(getTypeSpecificInfo(type));

            response.append("\n📅 *Calculé depuis les timestamps réels Oracle (createdAt → updatedAt)*");

            return new ChatResponse(response.toString(), "info", totalFiles);

        } catch (Exception e) {
            return new ChatResponse("Erreur lors du calcul des temps de traitement: " + e.getMessage(), "error", null);
        }
    }

    private ChatResponse getAllProcessingTimes(List<Fichier> allFiles) {
        try {
            Map<String, Long> typeStats = allFiles.stream()
                    .filter(f -> f.getTypeFichier() != null)
                    .collect(Collectors.groupingBy(
                            f -> f.getTypeFichier().name().toUpperCase(),
                            Collectors.counting()));

            StringBuilder response = new StringBuilder();
            response.append("⏱️ **Temps de Traitement - Vue Générale**\n\n");

            response.append("📊 **Répartition par Type:**\n");
            typeStats.forEach((type, count) -> {
                String emoji = getTypeEmoji(type);
                String avgTime = calculateAverageProcessingTime(type, count, 0, 0);
                response.append(String.format("%s %s: %d fichiers (Temps moyen: %s)\n",
                        emoji, type, count, avgTime));
            });

            response.append("\n📈 **Total Fichiers:** ").append(allFiles.size()).append("\n");
            response.append("\n📅 *Données mises à jour en temps réel depuis Oracle*");

            return new ChatResponse(response.toString(), "info", allFiles.size());

        } catch (Exception e) {
            return new ChatResponse("Erreur lors du calcul général des temps.", "error", null);
        }
    }

    private String calculateAverageProcessingTime(String type, long totalFiles, long todayCount, long weekCount) {
        // Algorithme de calcul basé sur le volume et le type
        switch (type.toUpperCase()) {
            case "CHEQUE":
                if (totalFiles > 1000)
                    return "2-3 jours (Volume élevé)";
                if (totalFiles > 500)
                    return "1-2 jours (Volume moyen)";
                return "1 jour (Volume faible)";

            case "VIREMENT":
                if (weekCount > 100)
                    return "Immédiat - 4h (Forte activité)";
                if (weekCount > 50)
                    return "Immédiat - 2h (Activité normale)";
                return "Immédiat (Faible activité)";

            case "PRELEVEMENT":
                if (todayCount > 20)
                    return "1-2 jours (Pic d'activité)";
                return "1 jour ouvrable";

            case "EFFET":
                if (totalFiles > 500)
                    return "2-3 jours (Volume important)";
                return "1-2 jours ouvrables";

            default:
                return "1-3 jours ouvrables";
        }
    }

    private String getTypeEmoji(String type) {
        switch (type.toUpperCase()) {
            case "CHEQUE":
                return "💳";
            case "VIREMENT":
                return "🔄";
            case "PRELEVEMENT":
                return "💰";
            case "EFFET":
                return "📄";
            default:
                return "📁";
        }
    }

    private String formatProcessingTime(double hours) {
        if (hours < 1) {
            long minutes = Math.round(hours * 60);
            return minutes + " minutes";
        } else if (hours < 24) {
            return String.format("%.1f heures", hours);
        } else {
            double days = hours / 24;
            if (days < 7) {
                return String.format("%.1f jours", days);
            } else {
                double weeks = days / 7;
                return String.format("%.1f semaines", weeks);
            }
        }
    }

    private String getProcessingTimeDistribution(List<Double> processingTimesHours) {
        if (processingTimesHours.isEmpty()) {
            return "• Aucune donnée disponible\n";
        }

        // Compter par tranches de temps
        long immediate = processingTimesHours.stream().mapToLong(h -> h < 1 ? 1 : 0).sum();
        long sameDay = processingTimesHours.stream().mapToLong(h -> h >= 1 && h < 24 ? 1 : 0).sum();
        long oneToThreeDays = processingTimesHours.stream().mapToLong(h -> h >= 24 && h < 72 ? 1 : 0).sum();
        long threePlusDays = processingTimesHours.stream().mapToLong(h -> h >= 72 ? 1 : 0).sum();

        long total = processingTimesHours.size();

        StringBuilder distribution = new StringBuilder();
        if (immediate > 0) {
            distribution.append(String.format("• < 1 heure: %d fichiers (%.1f%%)\n",
                    immediate, (immediate * 100.0) / total));
        }
        if (sameDay > 0) {
            distribution.append(String.format("• 1-24 heures: %d fichiers (%.1f%%)\n",
                    sameDay, (sameDay * 100.0) / total));
        }
        if (oneToThreeDays > 0) {
            distribution.append(String.format("• 1-3 jours: %d fichiers (%.1f%%)\n",
                    oneToThreeDays, (oneToThreeDays * 100.0) / total));
        }
        if (threePlusDays > 0) {
            distribution.append(String.format("• > 3 jours: %d fichiers (%.1f%%)\n",
                    threePlusDays, (threePlusDays * 100.0) / total));
        }

        return distribution.toString();
    }

    private String getTypeSpecificInfo(String type) {
        switch (type.toUpperCase()) {
            case "CHEQUE":
                return "📝 **Détails Chèques:**\n" +
                        "• Chèques locaux: Plus rapides\n" +
                        "• Chèques étrangers: Délais supplémentaires\n" +
                        "• Vérifications automatiques actives";

            case "VIREMENT":
                return "📝 **Détails Virements:**\n" +
                        "• Virements internes: Traitement immédiat\n" +
                        "• Virements SEPA: 1 jour ouvrable\n" +
                        "• Virements internationaux: 3-5 jours";

            case "PRELEVEMENT":
                return "📝 **Détails Prélèvements:**\n" +
                        "• Prélèvements SEPA: 1 jour ouvrable\n" +
                        "• Prélèvements récurrents: Automatiques\n" +
                        "• Contrôles de solvabilité inclus";

            case "EFFET":
                return "📝 **Détails Effets:**\n" +
                        "• Effets à l'encaissement: 2-3 jours\n" +
                        "• Effets à l'escompte: Validation rapide\n" +
                        "• Lettres de change: Traitement prioritaire";

            default:
                return "ℹ️ Informations générales de traitement";
        }
    }

    private ChatResponse getAttijaryAddress() {
        try {
            String response = "🏢 **Attijary Bank - Siège Social**\n\n" +
                    "📍 **Adresse:**\n" +
                    "24 Avenue Hédi Karray\n" +
                    "Tunis 1080, Tunisie\n\n" +
                    "📞 **Contact:**\n" +
                    "• Téléphone: +216 71 967 000\n" +
                    "• Fax: +216 71 967 099\n\n" +
                    "🌐 **Services:**\n" +
                    "• Direction Générale\n" +
                    "• Services Centraux\n" +
                    "• Support Client\n\n" +
                    "🕒 **Horaires:**\n" +
                    "Lundi - Vendredi: 08h00 - 17h00";

            return new ChatResponse(response, "info", null);

        } catch (Exception e) {
            return new ChatResponse("Erreur lors de la récupération de l'adresse.", "error", null);
        }
    }

    private ChatResponse getDefaultResponse() {
        return new ChatResponse(
                "🤖 **Assistant RU'ya à votre service!**\n\n" +
                        "Je peux vous aider avec:\n" +
                        "• 📊 Statistiques générales\n" +
                        "• 📅 Fichiers ajoutés aujourd'hui\n" +
                        "• ⏱️ Temps de traitement (chèques, virements, effets)\n" +
                        "• 🏢 Informations Attijary Bank\n" +
                        "• 💰 Analyses des montants\n" +
                        "• ❌ Fichiers rejetés\n" +
                        "• ✅ Fichiers remis\n" +
                        "• 👥 Statistiques utilisateurs\n\n" +
                        "💡 Posez-moi une question sur vos données financières!",
                "info",
                null);
    }
}
