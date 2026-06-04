package tn.esprit.ruya.services;

import tn.esprit.ruya.models.Dto;
import tn.esprit.ruya.models.Fichier;

import java.util.List;
import java.util.Optional;

public interface IFichierser {
    List<Fichier> getAllFichiers();
    Optional<Fichier> getFichierById(Long id);
    Fichier createFichier(Fichier fichier);
    Fichier updateFichier(Long id, Fichier updatedFichier);
    void deleteFichier(Long id);
    Dto getAllFichiersDto();
     List<Fichier> getAllFichiersByUser(Long id) ;

    }
