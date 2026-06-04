package tn.esprit.ruya.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.ruya.models.Fichier;

import java.util.List;

@Repository
public interface IFichierrepo extends JpaRepository<Fichier, Long> {

    List<Fichier> findByUserId(Long userId);

    // JPQL explicite pour Oracle et éviter tout souci de génération de requête
    @Query("select f from Fichier f where f.user.id = :userId")
    List<Fichier> findAllByUserId(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM FICHIERS WHERE ID_USER = :userId", nativeQuery = true)
    List<Fichier> findAllByUserIdNative(@Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM FICHIERS", nativeQuery = true)
    void deleteAllNative();

}
