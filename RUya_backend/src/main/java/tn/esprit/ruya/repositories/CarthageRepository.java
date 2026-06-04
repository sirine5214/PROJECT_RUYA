package tn.esprit.ruya.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.ruya.models.Carthago;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface CarthageRepository extends JpaRepository<Carthago, Long> {

        // === MÉTHODES DE BASE ===
        Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

        // Compter par code valeur (CHEQUE, EFFET, PRELEVEMENT, VIREMENT)
        Long countByCreatedAtBetweenAndCodeValeur(LocalDateTime start, LocalDateTime end,
                        Carthago.CodeValeur codeValeur);

        @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end")
        Double sumMontantByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

        // === MÉTHODES PAR TYPE_FICHIER ===
        Long countByCreatedAtBetweenAndTypeFichier(LocalDateTime start, LocalDateTime end, String typeFichier);

        @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.typeFichier = :type")
        Double sumMontantByCreatedAtBetweenAndTypeFichier(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end, @Param("type") String type);

        // === MÉTHODES PAR NATURE_FICHIER ===
        Long countByCreatedAtBetweenAndNatureFichier(LocalDateTime start, LocalDateTime end, String natureFichier);

        @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.natureFichier = :nature")
        Double sumMontantByCreatedAtBetweenAndNatureFichier(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end, @Param("nature") String nature);

        // === MÉTHODES PAR SENS ===
        Long countByCreatedAtBetweenAndSens(LocalDateTime start, LocalDateTime end, String sens);

        @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.sens = :sens")
        Double sumMontantByCreatedAtBetweenAndSens(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                        @Param("sens") String sens);

        // === MÉTHODES SESSION ET STATUT ===
        Long countByCreatedAtBetweenAndSessionDate(LocalDateTime start, LocalDateTime end, LocalDate sessionDate);

        Long countByCreatedAtBetweenAndStatutCheque(LocalDateTime start, LocalDateTime end, String statutCheque);

        Long countByCreatedAtBetweenAndStatutChequeAndCodeValeur(LocalDateTime start, LocalDateTime end,
                        String statutCheque, Carthago.CodeValeur codeValeur);

        @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.statutCheque = :statut")
        Double sumMontantByCreatedAtBetweenAndStatutCheque(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end, @Param("statut") String statut);

        // === MÉTHODES AVANT/APRÈS CTR ===
        Long countByCreatedAtBetweenAndAvantCTR(LocalDateTime start, LocalDateTime end, Boolean avantCTR);

        Long countByCreatedAtBetweenAndAvantCTRAndCodeValeur(LocalDateTime start, LocalDateTime end, Boolean avantCTR,
                        Carthago.CodeValeur codeValeur);

        Long countByCreatedAtBetweenAndApresCTR(LocalDateTime start, LocalDateTime end, Boolean apresCTR);

        Long countByCreatedAtBetweenAndApresCTRAndCodeValeur(LocalDateTime start, LocalDateTime end, Boolean apresCTR,
                        Carthago.CodeValeur codeValeur);

        Long countByCreatedAtBetweenAndStatutImage(LocalDateTime start, LocalDateTime end, Integer statutImage);

        Long countByCreatedAtBetweenAndTraiteParCTR(LocalDateTime start, LocalDateTime end, Boolean traiteParCTR);

        Long countByCreatedAtBetweenAndTraiteParCTRAndCodeValeur(LocalDateTime start, LocalDateTime end,
                        Boolean traiteParCTR, Carthago.CodeValeur codeValeur);

        @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.avantCTR = :avant")
        Double sumMontantByCreatedAtBetweenAndAvantCTR(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end, @Param("avant") Boolean avant);

        @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.apresCTR = :apres")
        Double sumMontantByCreatedAtBetweenAndApresCTR(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end, @Param("apres") Boolean apres);

        @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.traiteParCTR = :traite")
        Double sumMontantByCreatedAtBetweenAndTraiteParCTR(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end, @Param("traite") Boolean traite);

        // === MÉTHODES VALIDATION BO ===
        Long countByCreatedAtBetweenAndValideBodinars(LocalDateTime start, LocalDateTime end, Boolean valideBodinars);

        Long countByCreatedAtBetweenAndChequeWebBo(LocalDateTime start, LocalDateTime end, Boolean chequeWebBo);

        @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.valideBodinars = :valide")
        Double sumMontantByCreatedAtBetweenAndValideBodinars(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end, @Param("valide") Boolean valide);

        // === MÉTHODES REMISES ET ENV ===
        Long countByCreatedAtBetweenAndRemiseDouble(LocalDateTime start, LocalDateTime end, Boolean remiseDouble);

        Long countByCreatedAtBetweenAndFichierEnv(LocalDateTime start, LocalDateTime end, Boolean fichierEnv);

        Long countByCreatedAtBetweenAndFichierEnvAndCodeValeur(LocalDateTime start, LocalDateTime end,
                        Boolean fichierEnv, Carthago.CodeValeur codeValeur);

        @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.remiseDouble = :double")
        Double sumMontantByCreatedAtBetweenAndRemiseDouble(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end, @Param("double") Boolean remiseDouble);

        @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.fichierEnv = :env")
        Double sumMontantByCreatedAtBetweenAndFichierEnv(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end, @Param("env") Boolean env);

        // === MÉTHODES CONTRÔLES ET VÉRIFICATIONS ===
        @Query("SELECT COUNT(c) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.aVerifier = :verifier")
        Long countByCreatedAtBetweenAndAVerifier(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                        @Param("verifier") Boolean verifier);

        @Query("SELECT COUNT(c) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.controleEffectue = :controle")
        Long countByCreatedAtBetweenAndControleEffectue(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end, @Param("controle") Boolean controle);

        @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.aVerifier = :verifier")
        Double sumMontantByCreatedAtBetweenAndAVerifier(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end, @Param("verifier") Boolean verifier);

        // === MÉTHODES COMBINÉES POUR DASHBOARD ===
        @Query("SELECT COUNT(c) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end " +
                        "AND c.traiteParCTR = :traite AND c.statutCheque = :statut")
        Long countByCreatedAtBetweenAndTraiteParCTRAndStatutCheque(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end,
                        @Param("traite") Boolean traite,
                        @Param("statut") String statut);

        @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end " +
                        "AND c.traiteParCTR = :traite AND c.statutCheque = :statut")
        Double sumMontantByCreatedAtBetweenAndTraiteParCTRAndStatutCheque(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end,
                        @Param("traite") Boolean traite,
                        @Param("statut") String statut);

        @Query("SELECT COUNT(c) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end " +
                        "AND c.avantCTR = :avant AND c.natureFichier = :nature")
        Long countByCreatedAtBetweenAndAvantCTRAndNatureFichier(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end,
                        @Param("avant") Boolean avant,
                        @Param("nature") String nature);

        @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end " +
                        "AND c.avantCTR = :avant AND c.natureFichier = :nature")
        Double sumMontantByCreatedAtBetweenAndAvantCTRAndNatureFichier(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end,
                        @Param("avant") Boolean avant,
                        @Param("nature") String nature);

        @Query("SELECT COUNT(c) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end " +
                        "AND c.fichierEnv = :env AND c.apresCTR = :apres")
        Long countByCreatedAtBetweenAndFichierEnvAndApresCTR(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end,
                        @Param("env") Boolean env,
                        @Param("apres") Boolean apres);

        @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end " +
                        "AND c.fichierEnv = :env AND c.apresCTR = :apres")
        Double sumMontantByCreatedAtBetweenAndFichierEnvAndApresCTR(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end,
                        @Param("env") Boolean env,
                        @Param("apres") Boolean apres);

        // === MÉTHODES POUR FLUX CTR -> AMPLITUDE / AMPLITUDE ===
        @Query("SELECT COUNT(c) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end " +
                        "AND c.apresCTR = :apres AND c.typeFichier = :type")
        Long countByCreatedAtBetweenAndApresCTRAndTypeFichier(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end,
                        @Param("apres") Boolean apres,
                        @Param("type") Carthago.TypeFichier type);

        @Query("SELECT COUNT(c) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end " +
                        "AND c.apresCTR = :apres AND c.typeFichier = :type AND c.codeValeur = :code")
        Long countByCreatedAtBetweenAndApresCTRAndTypeFichierAndCodeValeur(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end,
                        @Param("apres") Boolean apres,
                        @Param("type") Carthago.TypeFichier type,
                        @Param("code") Carthago.CodeValeur code);

        @Query("SELECT COUNT(c) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end " +
                        "AND c.controleEffectue = :controle AND c.typeFichier = :type")
        Long countByCreatedAtBetweenAndControleEffectueAndTypeFichier(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end,
                        @Param("controle") Boolean controle,
                        @Param("type") Carthago.TypeFichier type);

        @Query("SELECT COUNT(c) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end " +
                        "AND c.controleEffectue = :controle AND c.typeFichier = :type AND c.codeValeur = :code")
        Long countByCreatedAtBetweenAndControleEffectueAndTypeFichierAndCodeValeur(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end,
                        @Param("controle") Boolean controle,
                        @Param("type") Carthago.TypeFichier type,
                        @Param("code") Carthago.CodeValeur code);

        @Query("SELECT COUNT(c) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end " +
                        "AND c.apresCTR = true AND c.statutCheque = :statut")
        Long countByCreatedAtBetweenAndApresCTRAndStatutCheque(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end,
                        @Param("statut") Carthago.StatutCheque statut);

        @Query("SELECT COUNT(c) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end " +
                        "AND c.apresCTR = true AND c.statutCheque = :statut AND c.codeValeur = :code")
        Long countByCreatedAtBetweenAndApresCTRAndStatutChequeAndCodeValeur(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end,
                        @Param("statut") Carthago.StatutCheque statut,
                        @Param("code") Carthago.CodeValeur code);

        // === COMBINAISONS COMPLEXES EXISTANTES ===
        Long countByCreatedAtBetweenAndTypeFichierAndTraiteParCTR(LocalDateTime start, LocalDateTime end,
                        String typeFichier, Boolean traiteParCTR);

        @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM Carthago c WHERE c.createdAt BETWEEN :start AND :end AND c.typeFichier = :type AND c.traiteParCTR = :traite")
        Double sumMontantByCreatedAtBetweenAndTypeFichierAndTraiteParCTR(@Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end, @Param("type") String type, @Param("traite") Boolean traite);
}