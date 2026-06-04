package tn.esprit.ruya.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.ruya.models.Fichier;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FichierRepository extends JpaRepository<Fichier, Long> {

    // === MÉTHODES DE BASE ===
    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end")
    Double sumMontantByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // === MÉTHODES PAR TYPE/NATURE/CODE ===
    Long countByCreatedAtBetweenAndTypeFichier(LocalDateTime start, LocalDateTime end, Fichier.TypeFichier typeFichier);

    Long countByCreatedAtBetweenAndNatureFichier(LocalDateTime start, LocalDateTime end, Fichier.NatureFichier natureFichier);

    Long countByCreatedAtBetweenAndCodeValeur(LocalDateTime start, LocalDateTime end, String codeValeur);

    // === COMBINAISONS POUR DASHBOARD ===
    Long countByCreatedAtBetweenAndTypeFichierAndNatureFichier(LocalDateTime start, LocalDateTime end, Fichier.TypeFichier typeFichier, Fichier.NatureFichier natureFichier);

    Long countByCreatedAtBetweenAndNatureFichierAndOrigineSaisie(LocalDateTime start, LocalDateTime end, Fichier.NatureFichier natureFichier, Fichier.OrigineSaisie origineSaisie);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.typeFichier = :type")
    Double sumMontantByCreatedAtBetweenAndTypeFichier(@Param("start") LocalDateTime start,
                                                      @Param("end") LocalDateTime end, @Param("type") Fichier.TypeFichier type);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.natureFichier = :nature")
    Double sumMontantByCreatedAtBetweenAndNatureFichier(@Param("start") LocalDateTime start,
                                                        @Param("end") LocalDateTime end, @Param("nature") Fichier.NatureFichier nature);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.codeValeur = :code")
    Double sumMontantByCreatedAtBetweenAndCodeValeur(@Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end, @Param("code") String code);

    // === MÉTHODES ORIGINE SAISIE ===
    Long countByCreatedAtBetweenAndOrigineSaisie(LocalDateTime start, LocalDateTime end, String origineSaisie);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.origineSaisie = :origine")
    Double sumMontantByCreatedAtBetweenAndOrigineSaisie(@Param("start") LocalDateTime start,
                                                        @Param("end") LocalDateTime end, @Param("origine") String origine);

    // === MÉTHODES GÉNÉRÉ PAR ENCAISSE ===
    @Query("SELECT COUNT(f) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.genereParEncaisse = :genere")
    Long countByCreatedAtBetweenAndGenereParEncaisse(@Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end, @Param("genere") Boolean genere);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.genereParEncaisse = :genere")
    Double sumMontantByCreatedAtBetweenAndGenereParEncaisse(@Param("start") LocalDateTime start,
                                                            @Param("end") LocalDateTime end, @Param("genere") Boolean genere);

    // === MÉTHODES VALIDATION BO ===
    @Query("SELECT COUNT(f) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.validation = :validation")
    Long countByCreatedAtBetweenAndValidationBO(@Param("start") LocalDateTime start,
                                                @Param("end") LocalDateTime end, @Param("validation") Boolean validation);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.validation = :validation")
    Double sumMontantByCreatedAtBetweenAndValidationBO(@Param("start") LocalDateTime start,
                                                       @Param("end") LocalDateTime end, @Param("validation") Boolean validation);

    // === MÉTHODES COMBINÉES POUR DASHBOARD ===
    @Query("SELECT COUNT(f) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end " +
            "AND f.genereParEncaisse = :genere AND f.validation = :valide")
    Long countByCreatedAtBetweenAndGenereParEncaisseAndValidationBO(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("genere") Boolean genere,
            @Param("valide") Boolean valide);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end " +
            "AND f.genereParEncaisse = :genere AND f.validation = :valide")
    Double sumMontantByCreatedAtBetweenAndGenereParEncaisseAndValidationBO(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("genere") Boolean genere,
            @Param("valide") Boolean valide);

    @Query("SELECT COUNT(f) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end " +
            "AND f.natureFichier = :nature AND f.genereParEncaisse = :genere AND f.validation = :valide")
    Long countByCreatedAtBetweenAndNatureFichierAndGenereParEncaisseAndValidationBO(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("nature") Fichier.NatureFichier nature,
            @Param("genere") Boolean genere,
            @Param("valide") Boolean valide);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end " +
            "AND f.natureFichier = :nature AND f.genereParEncaisse = :genere AND f.validation = :valide")
    Double sumMontantByCreatedAtBetweenAndNatureFichierAndGenereParEncaisseAndValidationBO(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("nature") Fichier.NatureFichier nature,
            @Param("genere") Boolean genere,
            @Param("valide") Boolean valide);

    @Query("SELECT COUNT(f) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end " +
            "AND f.natureFichier = :nature AND f.codeValeur = :code")
    Long countByCreatedAtBetweenAndNatureFichierAndCodeValeur(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("nature") Fichier.NatureFichier nature,
            @Param("code") String code);

    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end " +
            "AND f.natureFichier = :nature AND f.codeValeur = :code")
    Double sumMontantByCreatedAtBetweenAndNatureFichierAndCodeValeur(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("nature") Fichier.NatureFichier nature,
            @Param("code") String code);

    // === COMBINAISONS COMPLEXES EXISTANTES ===
    Long countByCreatedAtBetweenAndTypeFichierAndOrigineSaisie(LocalDateTime start, LocalDateTime end,
                                                               Fichier.TypeFichier typeFichier, String origineSaisie);

    @Query("SELECT COUNT(f) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end AND f.validation = :validation AND f.codeValeur = :code")
    Long countByCreatedAtBetweenAndValidationBOAndCodeValeur(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("validation") Boolean validation,
            @Param("code") String code);

    // === MÉTHODES POUR FICHIERSERVICE ===
    // Récupérer les fichiers par code valeur
    List<Fichier> findByCodeValeurOrderByCreatedAtDesc(String codeValeur);

    // Récupérer les fichiers par type et code valeur
    List<Fichier> findByTypeFichierAndCodeValeurOrderByCreatedAtDesc(Fichier.TypeFichier typeFichier,
                                                                     String codeValeur);

    // Récupérer les fichiers par code valeur et type (ordre inversé pour le service)
    List<Fichier> findByCodeValeurAndTypeFichierOrderByCreatedAtDesc(String codeValeur,
                                                                     Fichier.TypeFichier typeFichier);

    // Récupérer les fichiers par nature et code valeur
    List<Fichier> findByNatureFichierAndCodeValeurOrderByCreatedAtDesc(Fichier.NatureFichier natureFichier,
                                                                       String codeValeur);

    // Récupérer les fichiers par sens et code valeur
    List<Fichier> findBySensAndCodeValeurOrderByCreatedAtDesc(Fichier.Sens sens, String codeValeur);

    // Récupérer les fichiers par période et code valeur
    List<Fichier> findByCreatedAtBetweenAndCodeValeurOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end,
                                                                          String codeValeur);

    // Récupérer les fichiers par montant et code valeur
    List<Fichier> findByMontantAndCodeValeurOrderByCreatedAtDesc(Double montant, String codeValeur);

    // Récupérer les fichiers par validation et code valeur
    List<Fichier> findByValidationAndCodeValeurOrderByCreatedAtDesc(Boolean validation, String codeValeur);

    // Compter les fichiers par code valeur
    Long countByCodeValeur(String codeValeur);

    // Calculer le montant total par code valeur
    @Query("SELECT COALESCE(SUM(f.montant), 0.0) FROM Fichier f WHERE f.codeValeur = :codeValeur")
    Double sumMontantByCodeValeur(@Param("codeValeur") String codeValeur);

    // === MÉTHODES POUR COMPTER LES CHÈQUES (NOMBER) ===
    @Query("SELECT COALESCE(SUM(f.nomber), 0) FROM Fichier f WHERE f.createdAt BETWEEN :start AND :end " +
            "AND f.natureFichier = :nature AND f.codeValeur = :code")
    Long sumNomberByCreatedAtBetweenAndNatureFichierAndCodeValeur(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("nature") Fichier.NatureFichier nature,
            @Param("code") String code);
}