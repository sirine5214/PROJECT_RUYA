package tn.esprit.ruya.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.ruya.repositories.ICarthagoRepo;
import tn.esprit.ruya.models.Carthago;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CarthagoService {

    private final ICarthagoRepo carthagoRepo;

    public List<Carthago> getAll() {
        return carthagoRepo.findAll();
    }

    public Optional<Carthago> getById(Long id) {
        return carthagoRepo.findById(id);
    }

    public Carthago create(Carthago c) {
        return carthagoRepo.save(c);
    }

    public Carthago update(Long id, Carthago newC) {
        return carthagoRepo.findById(id).map(c -> {
            c.setNomFichier(newC.getNomFichier());
            c.setTypeFichier(newC.getTypeFichier());
            c.setNatureFichier(newC.getNatureFichier());
            c.setCodeValeur(newC.getCodeValeur());
            c.setSens(newC.getSens());
            c.setMontant(newC.getMontant());
            c.setNomber(newC.getNomber());
            c.setUser(newC.getUser());
            return carthagoRepo.save(c);
        }).orElse(null);
    }

    public void delete(Long id) {
        carthagoRepo.deleteById(id);
    }
}
