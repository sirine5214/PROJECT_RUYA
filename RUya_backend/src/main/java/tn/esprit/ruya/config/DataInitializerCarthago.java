package tn.esprit.ruya.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import tn.esprit.ruya.repositories.ICarthagoRepo;
import tn.esprit.ruya.repositories.IFichierrepo;
import tn.esprit.ruya.models.Carthago;
import tn.esprit.ruya.models.Fichier;
import tn.esprit.ruya.models.User;
import tn.esprit.ruya.repositories.IUserRepo;

import java.time.LocalDateTime;
import java.util.Random;

@Configuration
@RequiredArgsConstructor
public class DataInitializerCarthago {

    private final Random random = new Random();

    private String getCodeValeurForFichier(String type) {
        return switch (type.toLowerCase()) {
            case "cheque" -> "CHEQUE"; // Code texte (frontend supporte aussi codes numériques 30/31/32/33)
            case "effet" -> "EFFET"; // Code texte (frontend supporte aussi codes numériques 40/41)
            case "virement" -> "VIREMENT"; // Code texte (frontend supporte aussi code numérique 10)
            case "prelevement" -> "PRELEVEMENT"; // Code texte (frontend supporte aussi code numérique 20)
            default -> "CHEQUE";
        };
    }

    private Carthago.CodeValeur getCodeValeurForCarthago(String type) {
        return switch (type.toLowerCase()) {
            case "cheque" -> Carthago.CodeValeur.CHEQUE;
            case "effet" -> Carthago.CodeValeur.EFFET;
            case "virement" -> Carthago.CodeValeur.VIREMENT;
            case "prelevement" -> Carthago.CodeValeur.PRELEVEMENT;
            default -> Carthago.CodeValeur.CHEQUE; // Par défaut CHEQUE
        };
    }

    private Fichier.TypeFichier getTypeFichierForFichier(String type) {
        return switch (type.toLowerCase()) {
            case "cheque" -> Fichier.TypeFichier.ELECTRONIQUE;
            case "effet" -> Fichier.TypeFichier.MANUEL;
            case "virement" -> Fichier.TypeFichier.WEB;
            case "prelevement" -> Fichier.TypeFichier.ELECTRONIQUE;
            default -> Fichier.TypeFichier.EN_SAISIE;
        };
    }

    private Carthago.TypeFichier getTypeFichierForCarthago(String type) {
        return switch (type.toLowerCase()) {
            case "cheque" -> Carthago.TypeFichier.ELECTRONIQUE;
            case "effet" -> Carthago.TypeFichier.MANUEL;
            case "virement" -> Carthago.TypeFichier.WEB;
            case "prelevement" -> Carthago.TypeFichier.ELECTRONIQUE;
            default -> Carthago.TypeFichier.WEB;
        };
    }

    @Bean
    @Order(2) // Exécuté après DataInitializer
    CommandLineRunner fillFakeData(IFichierrepo fichierRepo, ICarthagoRepo carthagoRepo, IUserRepo userRepo) {
        return args -> {
            // 🗑️ NETTOYER LES ANCIENNES DONNÉES avec requête native
            System.out.println("🗑️ Suppression des anciennes données de test...");
            try {
                carthagoRepo.deleteAllNative();
                fichierRepo.deleteAllNative();
                System.out.println("✅ Anciennes données supprimées");
            } catch (Exception e) {
                System.out.println("⚠️ Erreur lors de la suppression (normal si tables vides): " + e.getMessage());
            }

            User user = userRepo.findById(1L).orElse(null);
            if (user == null) {
                System.out.println("⚠️ Aucun utilisateur trouvé !");
                return;
            }

            String[] types = { "cheque", "effet", "virement", "prelevement" };
            String[] natures = { "env", "rcp" };
            String[] sensList = { "emis", "recu" };

            // Remplir FICHIER - REMISES EV de CHEQUES
            LocalDateTime now = LocalDateTime.now();

            // Créer fichiers CHEQUE avec codes variés (30, 31, 32, 33)
            String[] codeCheque = { "30", "31", "32", "33" };
            String[] codeLabel = { "REMIS", "EN_COURS", "REJETE", "RENDU" };

            for (int i = 0; i < 12; i++) {
                int codeIndex = i % 4;
                Fichier f = new Fichier();
                f.setNomFichier("cheque_" + codeLabel[codeIndex].toLowerCase() + "_" + String.format("%04d", i + 1));
                f.setTypeFichier(Fichier.TypeFichier.ELECTRONIQUE);
                f.setNatureFichier(Fichier.NatureFichier.REMISE);
                f.setSens(Fichier.Sens.ENTRANT);
                f.setCodeValeur(codeCheque[codeIndex]); // Codes: 30, 31, 32, 33
                f.setCodEn("21");
                f.setMontant(50000.0 + (i * 10000));
                f.setNomber(10 + (i * 2));
                f.setUser(user);
                f.setCreatedAt(now);
                f.setUpdatedAt(now);
                f.setValidation(true);
                f.setGenereParEncaisse(true);
                f.setOrigineSaisie(Fichier.OrigineSaisie.WEB);
                f.setTypeEncaissement(Fichier.TypeEncaissement.IMMEDIAT);
                f.setNumeroRemise("REM-CHQ" + codeCheque[codeIndex] + "-" + String.format("%04d", i + 1));

                fichierRepo.save(f);
            }

            // Créer fichiers EFFET avec codes 40 et 41
            for (int i = 0; i < 6; i++) {
                String codeEffet = (i % 2 == 0) ? "40" : "41";
                Fichier f = new Fichier();
                f.setNomFichier("effet_" + codeEffet + "_" + String.format("%04d", i + 1));
                f.setTypeFichier(Fichier.TypeFichier.MANUEL);
                f.setNatureFichier(Fichier.NatureFichier.FICHIER);
                f.setSens(Fichier.Sens.ENTRANT);
                f.setCodeValeur(codeEffet); // Codes: 40 ou 41
                f.setCodEn("21");
                f.setMontant(8000.0 + (i * 1000));
                f.setNomber(5 + i);
                f.setUser(user);
                f.setCreatedAt(now);
                f.setUpdatedAt(now);
                f.setValidation(false);
                f.setGenereParEncaisse(false);
                f.setOrigineSaisie(Fichier.OrigineSaisie.WEB);
                f.setTypeEncaissement(Fichier.TypeEncaissement.IMMEDIAT);
                fichierRepo.save(f);
            }

            // Créer fichiers VIREMENT code 10
            for (int i = 0; i < 3; i++) {
                Fichier f = new Fichier();
                f.setNomFichier("virement_10_" + String.format("%04d", i + 1));
                f.setTypeFichier(Fichier.TypeFichier.WEB);
                f.setNatureFichier(Fichier.NatureFichier.FICHIER);
                f.setSens(Fichier.Sens.ENTRANT);
                f.setCodeValeur("10");
                f.setCodEn("21");
                f.setMontant(15000.0 + (i * 5000));
                f.setNomber(3 + i);
                f.setUser(user);
                f.setCreatedAt(now);
                f.setUpdatedAt(now);
                f.setValidation(false);
                f.setGenereParEncaisse(false);
                f.setOrigineSaisie(Fichier.OrigineSaisie.WEB);
                f.setTypeEncaissement(Fichier.TypeEncaissement.IMMEDIAT);
                fichierRepo.save(f);
            }

            // Créer fichiers PRELEVEMENT code 20
            for (int i = 0; i < 3; i++) {
                Fichier f = new Fichier();
                f.setNomFichier("prelevement_20_" + String.format("%04d", i + 1));
                f.setTypeFichier(Fichier.TypeFichier.ELECTRONIQUE);
                f.setNatureFichier(Fichier.NatureFichier.FICHIER);
                f.setSens(Fichier.Sens.ENTRANT);
                f.setCodeValeur("20");
                f.setCodEn("21");
                f.setMontant(10000.0 + (i * 2000));
                f.setNomber(4 + i);
                f.setUser(user);
                f.setCreatedAt(now);
                f.setUpdatedAt(now);
                f.setValidation(false);
                f.setGenereParEncaisse(false);
                f.setOrigineSaisie(Fichier.OrigineSaisie.WEB);
                f.setTypeEncaissement(Fichier.TypeEncaissement.IMMEDIAT);
                fichierRepo.save(f);
            }

            System.out.println(
                    "✅ Table FICHIER remplie: 12 chèques (30/31/32/33), 6 effets (40/41), 3 virements (10), 3 prélèvements (20)"); // Remplir
                                                                                                                                   // CARTHAGO
                                                                                                                                   // -
                                                                                                                                   // CHEQUES
                                                                                                                                   // INDIVIDUELS
                                                                                                                                   // selon
                                                                                                                                   // le
                                                                                                                                   // flux
                                                                                                                                   // du
                                                                                                                                   // dashboard
            LocalDateTime now2 = LocalDateTime.now();

            // FLUX: FICHIER (5 Remises) → CARTHAGO (50 chèques) → CTR → AMPLITUDE
            // Chaque remise contient environ 10 chèques = 50 chèques au total

            for (int i = 0; i < 50; i++) {
                Carthago c = new Carthago();
                c.setNomFichier("cheque_" + String.format("%05d", i + 1));
                c.setCodeValeur(Carthago.CodeValeur.CHEQUE);
                c.setCodEn("22");
                c.setMontant(1000.0 + (i * 100));
                c.setNomber(1); // 1 chèque par enregistrement
                c.setUser(user);
                c.setCreatedAt(now2);
                c.setUpdatedAt(now2);
                c.setNatureFichier(Carthago.NatureFichier.FICHIER);
                c.setSens(Carthago.Sens.ENTRANT);

                // Répartition selon le flux du dashboard:
                // 0-2: Remises EV non parvenues CARTHAGO (fichierEnv=false)
                if (i < 3) {
                    c.setTypeFichier(Carthago.TypeFichier.ELECTRONIQUE);
                    c.setAvantCTR(true);
                    c.setApresCTR(false);
                    c.setTraiteParCTR(false);
                    c.setFichierEnv(false); // NON PARVENUES
                    c.setStatutCheque(Carthago.StatutCheque.PENDING);
                }
                // 3-7: Remises EV non traitées CARTHAGO (avantCTR=true, traiteParCTR=false)
                else if (i < 8) {
                    c.setTypeFichier(Carthago.TypeFichier.ELECTRONIQUE);
                    c.setAvantCTR(true);
                    c.setApresCTR(false);
                    c.setTraiteParCTR(false);
                    c.setFichierEnv(true);
                    c.setStatutCheque(Carthago.StatutCheque.EN_COURS);
                }
                // 8-22: Remises EV traitées → CTR (15 ELECTRONIQUES INTRA)
                else if (i < 23) {
                    c.setTypeFichier(Carthago.TypeFichier.ELECTRONIQUE);
                    c.setAvantCTR(false);
                    c.setApresCTR(true);
                    c.setTraiteParCTR(true);
                    c.setFichierEnv(true);
                    c.setStatutCheque(i == 22 ? Carthago.StatutCheque.REJETE : Carthago.StatutCheque.TRAITE);
                    c.setControleEffectue(false);
                }
                // 23-32: Remises AGC → CTR (10 MANUELS INTER)
                else if (i < 33) {
                    c.setTypeFichier(Carthago.TypeFichier.MANUEL);
                    c.setAvantCTR(false);
                    c.setApresCTR(true);
                    c.setTraiteParCTR(true);
                    c.setFichierEnv(true);
                    c.setStatutCheque(Carthago.StatutCheque.TRAITE);
                    c.setControleEffectue(false);
                }
                // 33-42: Amplitude INTRA (10 ELECTRONIQUES contrôlés)
                else if (i < 43) {
                    c.setTypeFichier(Carthago.TypeFichier.ELECTRONIQUE);
                    c.setAvantCTR(false);
                    c.setApresCTR(true);
                    c.setTraiteParCTR(true);
                    c.setFichierEnv(true);
                    c.setControleEffectue(true); // Contrôle effectué = AMPLITUDE
                    c.setStatutCheque(Carthago.StatutCheque.TRAITE);
                }
                // 43-49: Amplitude INTER (7 MANUELS contrôlés)
                else {
                    c.setTypeFichier(Carthago.TypeFichier.MANUEL);
                    c.setAvantCTR(false);
                    c.setApresCTR(true);
                    c.setTraiteParCTR(true);
                    c.setFichierEnv(true);
                    c.setControleEffectue(true); // Contrôle effectué = AMPLITUDE
                    c.setStatutCheque(Carthago.StatutCheque.TRAITE);
                }

                carthagoRepo.save(c);
            }

            // Créer quelques enregistrements pour les autres types (effet, virement,
            // prelevement)
            for (int i = 0; i < 10; i++) {
                String type = types[(i % 3) + 1]; // effet, virement, prelevement
                Carthago c = new Carthago();
                c.setNomFichier("carthago_" + type + "_" + String.format("%04d", i + 1));
                c.setTypeFichier(getTypeFichierForCarthago(type));
                c.setNatureFichier(Carthago.NatureFichier.FICHIER);
                c.setSens(Carthago.Sens.ENTRANT);
                c.setCodeValeur(getCodeValeurForCarthago(type));
                c.setCodEn("22");
                c.setMontant(5000.0 + (i * 500));
                c.setNomber(2 + i);
                c.setUser(user);
                c.setCreatedAt(now);
                c.setUpdatedAt(now);
                c.setAvantCTR(false);
                c.setApresCTR(true);
                c.setTraiteParCTR(true);
                c.setFichierEnv(true);
                c.setStatutCheque(Carthago.StatutCheque.TRAITE);

                carthagoRepo.save(c);
            }

            System.out.println("✅ Table CARTHAGO remplie avec 50 CHEQUES + 10 autres types");
            System.out.println("📊 Répartition des 50 CHEQUES:");
            System.out.println("   - 3 non parvenues CARTHAGO");
            System.out.println("   - 5 non traitées CARTHAGO");
            System.out.println("   - 15 EV → CTR (ELECTRONIQUES/INTRA)");
            System.out.println("   - 10 AGC → CTR (MANUELS/INTER)");
            System.out.println("   - 10 AMPLITUDE INTRA (ELECTRONIQUES contrôlés)");
            System.out.println("   - 7 AMPLITUDE INTER (MANUELS contrôlés)");
        };
    }
}
