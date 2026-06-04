package tn.esprit.ruya.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.ruya.models.CTR;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CtrRepository extends JpaRepository<CTR, Long> {

    // === MÉTHODES DE COMPTAGE DE BASE ===
    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    Long countByCreatedAtBetweenAndStatutCtr(LocalDateTime start, LocalDateTime end, String statutCtr);
    Long countByCreatedAtBetweenAndTypeOperation(LocalDateTime start, LocalDateTime end, String typeOperation);
    Long countByCreatedAtBetweenAndEquilibre(LocalDateTime start, LocalDateTime end, Boolean equilibre);
    Long countByCreatedAtBetweenAndRemiseDouble(LocalDateTime start, LocalDateTime end, Boolean remiseDouble);
    Long countByCreatedAtBetweenAndRemiseNonParvenue(LocalDateTime start, LocalDateTime end, Boolean remiseNonParvenue);

    // === MÉTHODES DE SOMME MONTANTS ===
    @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM CTR c WHERE c.createdAt BETWEEN :start AND :end")
    Double sumMontantByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(c.montant), 0.0) FROM CTR c WHERE c.createdAt BETWEEN :start AND :end AND c.statutCtr = :statut")
    Double sumMontantByCreatedAtBetweenAndStatutCtr(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("statut") String statut);

    @Query("SELECT COALESCE(SUM(c.montantCarthago), 0.0) FROM CTR c WHERE c.createdAt BETWEEN :start AND :end")
    Double sumMontantCarthagoByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(c.montantFichiers), 0.0) FROM CTR c WHERE c.createdAt BETWEEN :start AND :end")
    Double sumMontantFichiersByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // === MÉTHODES DE SOMME NOMBRES ===
    @Query("SELECT COALESCE(SUM(c.nombreCarthago), 0) FROM CTR c WHERE c.createdAt BETWEEN :start AND :end")
    Integer sumNombreCarthagoByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(c.nombreFichiers), 0) FROM CTR c WHERE c.createdAt BETWEEN :start AND :end")
    Integer sumNombreFichiersByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(c.nombreElements), 0) FROM CTR c WHERE c.createdAt BETWEEN :start AND :end")
    Integer sumNombreElementsByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // === MÉTHODES D'ÉQUILIBRAGE ===
    @Query("SELECT COUNT(c) FROM CTR c WHERE c.createdAt BETWEEN :start AND :end AND c.equilibre = true")
    Long countEquilibreByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(c.difference), 0.0) FROM CTR c WHERE c.createdAt BETWEEN :start AND :end")
    Double sumDifferenceByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // === MÉTHODES DE RECHERCHE ===
    List<CTR> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // === MÉTHODES SUPPLÉMENTAIRES POUR ANALYSES ===
    @Query("SELECT COUNT(c) FROM CTR c WHERE c.createdAt BETWEEN :start AND :end AND c.codeValeur = :code")
    Long countByCreatedAtBetweenAndCodeValeur(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("code") String code);

    @Query("SELECT COUNT(c) FROM CTR c WHERE c.createdAt BETWEEN :start AND :end AND c.genereVersCtr = :genere")
    Long countByCreatedAtBetweenAndGenereVersCtr(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("genere") Boolean genere);

    @Query("SELECT COUNT(c) FROM CTR c WHERE c.createdAt BETWEEN :start AND :end AND c.recuParCtr = :recu")
    Long countByCreatedAtBetweenAndRecuParCtr(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("recu") Boolean recu);

    // === INDICATEURS SPÉCIFIQUES ===
    Long countByCreatedAtBetweenAndChequeElectroniqueCtr(LocalDateTime start, LocalDateTime end, Boolean chequeElectroniqueCtr);

    Long countByCreatedAtBetweenAndFichierEnvCtr(LocalDateTime start, LocalDateTime end, Boolean fichierEnvCtr);
}