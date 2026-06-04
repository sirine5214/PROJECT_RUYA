package tn.esprit.ruya.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.ruya.models.Amplitude;

import java.time.LocalDateTime;

@Repository
public interface AmplitudeRepository extends JpaRepository<Amplitude, Long> {

    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    Long countByCreatedAtBetweenAndTypeCheque(LocalDateTime start, LocalDateTime end, Amplitude.TypeCheque typeCheque);

    Long countByCreatedAtBetweenAndStatutTraitement(LocalDateTime start, LocalDateTime end, Amplitude.StatutTraitement statutTraitement);

    @Query("SELECT COALESCE(SUM(a.montant), 0.0) FROM Amplitude a WHERE a.createdAt BETWEEN :start AND :end")
    Double sumMontantByCreatedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}


