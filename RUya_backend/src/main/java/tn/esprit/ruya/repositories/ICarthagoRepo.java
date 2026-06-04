package tn.esprit.ruya.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.ruya.models.Carthago;

public interface ICarthagoRepo extends JpaRepository<Carthago, Long> {
    
    @Transactional
    @Modifying
    @Query(value = "DELETE FROM CARTHAGO", nativeQuery = true)
    void deleteAllNative();
}
