
package tn.esprit.ruya.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.ruya.models.*;
import tn.esprit.ruya.repositories.CarthageRepository;
import tn.esprit.ruya.repositories.FichierRepository;
import tn.esprit.ruya.repositories.CtrRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {

    @Autowired
    public CarthageRepository carthagoRepository;

    @Autowired
    public FichierRepository fichierRepository;

    @Autowired
    public CtrRepository ctrRepository;

    public DashboardResponseDTO getDashboardDataCorrected() {
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).minusSeconds(1);
        return getDashboardDataForPeriodCorrected(today, endOfDay);
    }

    public DashboardResponseDTO getDashboardDataForPeriodCorrected(LocalDateTime start, LocalDateTime end) {
        DashboardResponseDTO response = new DashboardResponseDTO();
        List<CardDataDTO> cardData = new ArrayList<>();

        cardData.add(buildEncaisseValeurCard(start, end));         // 1 - Encaisse Valeur (EV)
        cardData.add(buildTotalCarthageCard(start, end));          // 2 - Total Carthago
        cardData.add(buildCarthagoCTRCard(start, end));            // 3 - Carthago → CTR
        cardData.add(buildRemisesNonParvenuesCTRCard(start, end)); // 4 - Remises non parvenues CTR
        cardData.add(buildTotalCTRCard(start, end));               // 5 - Total CTR
        cardData.add(buildDepotCTRCard(start, end));               // 6 - Dépôt CTR
        cardData.add(buildCTRAmplitudeCard(start, end));           // 7 - CTR → Amplitude
        cardData.add(buildAmplitudeCard(start, end));              // 8 - Amplitude

        response.setCardData(cardData);
        response.setGlobalStats(buildGlobalStats(start, end));
        response.setLastUpdate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        response.setConnected(true);

        return response;
    }

    /**
     * 1️⃣ ENCAISSE VALEUR (EV)
     * Résumé des opérations EV pour la session du jour
     */
    private CardDataDTO buildEncaisseValeurCard(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Encaisse Valeur - CHEQUES");
        card.setIcon("fas fa-money-bill-wave");
        card.setType("primary");

        List<DataRowDTO> data = new ArrayList<>();
        try {
            // Nbr Remise = nombre de REMISES dans FICHIER (cherche "CHEQUE" OU "30")
            Long nbRemiseEV = fichierRepository.countByCreatedAtBetweenAndNatureFichierAndCodeValeur(
                start, end, Fichier.NatureFichier.REMISE, "CHEQUE");

            // Nbr Chèque = SOMME du champ nomber de toutes les remises CHEQUE dans FICHIER
            Long nbChequesTotal = fichierRepository.sumNomberByCreatedAtBetweenAndNatureFichierAndCodeValeur(
                start, end, Fichier.NatureFichier.REMISE, "CHEQUE");

            // Remises non parvenues = compter les CHÈQUES avec fichierEnv=false
            Long chequesNonParvenus = carthagoRepository.countByCreatedAtBetweenAndFichierEnvAndCodeValeur(
                start, end, false, Carthago.CodeValeur.CHEQUE);

            // Remises non traitées = compter les CHÈQUES avec avantCTR=true ET traiteParCTR=false
            Long chequesNonTraites = carthagoRepository.countByCreatedAtBetweenAndAvantCTRAndCodeValeur(
                start, end, true, Carthago.CodeValeur.CHEQUE);

            data.add(new DataRowDTO("Nbr Remise", String.valueOf(nbRemiseEV == null ? 0 : nbRemiseEV), null, "primary"));
            data.add(new DataRowDTO("Nbr Chèque", String.valueOf(nbChequesTotal == null ? 0 : nbChequesTotal), null, null));
            data.add(new DataRowDTO("Nbr Remise EV Non Parvenues CARTHAGO", "0", null, "warning"));
            data.add(new DataRowDTO("Nbr Chèque", String.valueOf(chequesNonParvenus == null ? 0 : chequesNonParvenus), null, null));
            data.add(new DataRowDTO("Nbr Remise EV Non Traitée CARTHAGO", "0", null, "warning"));
            data.add(new DataRowDTO("Nbr Chèque", String.valueOf(chequesNonTraites == null ? 0 : chequesNonTraites), null, null));

        } catch (Exception e) {
            System.err.println("❌ Erreur buildEncaisseValeurCard: " + e.getMessage());
            e.printStackTrace();
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }
        card.setData(data);
        return card;
    }

    /**
     * 2️⃣ TOTAL CARTHAGO
     * Total des chèques dans CARTHAGO par type
     */
    private CardDataDTO buildTotalCarthageCard(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Total Carthago - CHEQUES");
        card.setIcon("fas fa-database");
        card.setType("info");

        List<DataRowDTO> data = new ArrayList<>();
        try {
            // Chèques EV (ELECTRONIQUES)
            Long nbChequesEV = carthagoRepository.countByCreatedAtBetweenAndApresCTRAndTypeFichierAndCodeValeur(
                start, end, true, Carthago.TypeFichier.ELECTRONIQUE, Carthago.CodeValeur.CHEQUE);

            // Chèques AGC (MANUELS)
            Long nbChequesAGC = carthagoRepository.countByCreatedAtBetweenAndApresCTRAndTypeFichierAndCodeValeur(
                start, end, true, Carthago.TypeFichier.MANUEL, Carthago.CodeValeur.CHEQUE);

            data.add(new DataRowDTO("Nbr Remise EV", "0", null, "success"));
            data.add(new DataRowDTO("Nbr Chèque", String.valueOf(nbChequesEV == null ? 0 : nbChequesEV), null, null));
            data.add(new DataRowDTO("Nbr Remise AGC", "0", null, null));
            data.add(new DataRowDTO("Nbr Chèque", String.valueOf(nbChequesAGC == null ? 0 : nbChequesAGC), null, null));

        } catch (Exception e) {
            System.err.println("❌ Erreur buildTotalCarthageCard: " + e.getMessage());
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }
        card.setData(data);
        return card;
    }

    /**
     * 3️⃣ CARTHAGO → CTR
     * Flux de chèques transférés de CARTHAGO vers CTR
     */
    private CardDataDTO buildCarthagoCTRCard(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Carthago → CTR - CHEQUES");
        card.setIcon("fas fa-arrow-right");
        card.setType("success");

        List<DataRowDTO> data = new ArrayList<>();
        try {
            // Chèques EV transférés (apresCTR=true, typeFichier=ELECTRONIQUE)
            Long nbChequesEV = carthagoRepository.countByCreatedAtBetweenAndApresCTRAndTypeFichierAndCodeValeur(
                start, end, true, Carthago.TypeFichier.ELECTRONIQUE, Carthago.CodeValeur.CHEQUE);

            // Chèques AGC transférés (apresCTR=true, typeFichier=MANUEL)
            Long nbChequesAGC = carthagoRepository.countByCreatedAtBetweenAndApresCTRAndTypeFichierAndCodeValeur(
                start, end, true, Carthago.TypeFichier.MANUEL, Carthago.CodeValeur.CHEQUE);

            data.add(new DataRowDTO("Nbr Remise EV", "0", null, "success"));
            data.add(new DataRowDTO("Nbr Chèque", String.valueOf(nbChequesEV == null ? 0 : nbChequesEV), null, null));
            data.add(new DataRowDTO("Nbr Remise AGC", "0", null, null));
            data.add(new DataRowDTO("Nbr Chèque", String.valueOf(nbChequesAGC == null ? 0 : nbChequesAGC), null, null));

        } catch (Exception e) {
            System.err.println("❌ Erreur buildCarthagoCTRCard: " + e.getMessage());
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }
        card.setData(data);
        return card;
    }

    /**
     * 4️⃣ REMISES NON PARVENUES CTR
     * Anomalies - chèques non parvenus au CTR
     */
    private CardDataDTO buildRemisesNonParvenuesCTRCard(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Remises non parvenues CTR");
        card.setIcon("fas fa-exclamation-triangle");
        card.setType("warning");

        List<DataRowDTO> data = new ArrayList<>();
        try {
            // Chèques AG non parvenus (fichierEnv=false)
            Long chequesAgNonParvenus = carthagoRepository.countByCreatedAtBetweenAndFichierEnvAndCodeValeur(
                start, end, false, Carthago.CodeValeur.CHEQUE);

            data.add(new DataRowDTO("Nbr Remise EV non parvenues CTR", "0", null, "warning"));
            data.add(new DataRowDTO("Nbr Chèque", "0", null, null));
            data.add(new DataRowDTO("Nbr Remise AG non parvenues CTR", "0", null, "warning"));
            data.add(new DataRowDTO("Nbr Chèque", String.valueOf(chequesAgNonParvenus == null ? 0 : chequesAgNonParvenus), null, null));

        } catch (Exception e) {
            System.err.println("❌ Erreur buildRemisesNonParvenuesCTRCard: " + e.getMessage());
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }
        card.setData(data);
        return card;
    }

    /**
     * 5️⃣ TOTAL CTR
     * Total des opérations comptabilisées dans le système CTR
     */
    private CardDataDTO buildTotalCTRCard(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Total CTR - CHEQUES");
        card.setIcon("fas fa-server");
        card.setType("info");

        List<DataRowDTO> data = new ArrayList<>();
        try {
            // Total des remises validées dans CTR
            Long nbRemiseCTR = ctrRepository.countByCreatedAtBetweenAndCodeValeur(start, end, "CHEQUE");

            // Chèques manuels dans CTR
            Long nbChequeManu = ctrRepository.countByCreatedAtBetweenAndChequeElectroniqueCtr(start, end, false);

            data.add(new DataRowDTO("Nbr Remise", String.valueOf(nbRemiseCTR == null ? 0 : nbRemiseCTR), null, "primary"));
            data.add(new DataRowDTO("Nbr Chèque manuel", String.valueOf(nbChequeManu == null ? 0 : nbChequeManu), null, "warning"));

        } catch (Exception e) {
            System.err.println("❌ Erreur buildTotalCTRCard: " + e.getMessage());
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }
        card.setData(data);
        return card;
    }

    /**
     * 6️⃣ DÉPÔT CTR
     * Informations sur les dépôts CTR par type
     */
    private CardDataDTO buildDepotCTRCard(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Dépôt CTR - CHEQUES");
        card.setIcon("fas fa-archive");
        card.setType("default");

        List<DataRowDTO> data = new ArrayList<>();
        try {
            // Chèques INTRA (électroniques, après CTR)
            Long nbChequeINTRA = carthagoRepository.countByCreatedAtBetweenAndApresCTRAndTypeFichierAndCodeValeur(
                start, end, true, Carthago.TypeFichier.ELECTRONIQUE, Carthago.CodeValeur.CHEQUE);

            // Chèques INTER (manuels, après CTR)
            Long nbChequeINTER = carthagoRepository.countByCreatedAtBetweenAndApresCTRAndTypeFichierAndCodeValeur(
                start, end, true, Carthago.TypeFichier.MANUEL, Carthago.CodeValeur.CHEQUE);

            // Chèques Manuel (fichiers envoyés)
            Long nbChequeManuel = carthagoRepository.countByCreatedAtBetweenAndFichierEnvAndCodeValeur(
                start, end, true, Carthago.CodeValeur.CHEQUE);

            // Total
            long nbChequeTotal = (nbChequeINTRA == null ? 0 : nbChequeINTRA) + 
                                (nbChequeINTER == null ? 0 : nbChequeINTER);

            data.add(new DataRowDTO("Nbr Chèque INTRA", String.valueOf(nbChequeINTRA == null ? 0 : nbChequeINTRA), null, null));
            data.add(new DataRowDTO("Nbr Chèque INTER", String.valueOf(nbChequeINTER == null ? 0 : nbChequeINTER), null, null));
            data.add(new DataRowDTO("Nbr Chèque Manuel", String.valueOf(nbChequeManuel == null ? 0 : nbChequeManuel), null, null));
            data.add(new DataRowDTO("Nbr Chèque Total", String.valueOf(nbChequeTotal), null, "success"));

        } catch (Exception e) {
            System.err.println("❌ Erreur buildDepotCTRCard: " + e.getMessage());
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }
        card.setData(data);
        return card;
    }

    /**
     * 7️⃣ CTR → AMPLITUDE
     * Flux vers la plateforme Amplitude
     */
    private CardDataDTO buildCTRAmplitudeCard(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("CTR → Amplitude - CHEQUES");
        card.setIcon("fas fa-chart-line");
        card.setType("default");

        List<DataRowDTO> data = new ArrayList<>();
        try {
            // Chèques INTRA (électroniques, après CTR)
            Long nbChequeINTRA = carthagoRepository.countByCreatedAtBetweenAndApresCTRAndTypeFichierAndCodeValeur(
                start, end, true, Carthago.TypeFichier.ELECTRONIQUE, Carthago.CodeValeur.CHEQUE);

            // Chèques INTER (manuels, après CTR)
            Long nbChequeINTER = carthagoRepository.countByCreatedAtBetweenAndApresCTRAndTypeFichierAndCodeValeur(
                start, end, true, Carthago.TypeFichier.MANUEL, Carthago.CodeValeur.CHEQUE);

            // Total
            long nbChequeTotal = (nbChequeINTRA == null ? 0 : nbChequeINTRA) + 
                                (nbChequeINTER == null ? 0 : nbChequeINTER);

            data.add(new DataRowDTO("Nbr Chèque INTRA", String.valueOf(nbChequeINTRA == null ? 0 : nbChequeINTRA), null, null));
            data.add(new DataRowDTO("Nbr Chèque INTER", String.valueOf(nbChequeINTER == null ? 0 : nbChequeINTER), null, null));
            data.add(new DataRowDTO("Nbr Chèque Total", String.valueOf(nbChequeTotal), null, "success"));

        } catch (Exception e) {
            System.err.println("❌ Erreur buildCTRAmplitudeCard: " + e.getMessage());
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }
        card.setData(data);
        return card;
    }

    /**
     * 8️⃣ AMPLITUDE
     * Données internes d'Amplitude (étape finale du flux)
     */
    private CardDataDTO buildAmplitudeCard(LocalDateTime start, LocalDateTime end) {
        CardDataDTO card = new CardDataDTO();
        card.setTitle("Amplitude - CHEQUES");
        card.setIcon("fas fa-wave-square");
        card.setType("success");

        List<DataRowDTO> data = new ArrayList<>();
        try {
            // Chèques INTRA dans Amplitude (contrôle effectué, électroniques)
            Long nbChequeINTRA = carthagoRepository.countByCreatedAtBetweenAndControleEffectueAndTypeFichierAndCodeValeur(
                start, end, true, Carthago.TypeFichier.ELECTRONIQUE, Carthago.CodeValeur.CHEQUE);

            // Chèques INTER dans Amplitude (contrôle effectué, manuels)
            Long nbChequeINTER = carthagoRepository.countByCreatedAtBetweenAndControleEffectueAndTypeFichierAndCodeValeur(
                start, end, true, Carthago.TypeFichier.MANUEL, Carthago.CodeValeur.CHEQUE);

            // Total
            long nbChequeTotal = (nbChequeINTRA == null ? 0 : nbChequeINTRA) + 
                                (nbChequeINTER == null ? 0 : nbChequeINTER);

            // Rejet d'intégration
            Long rejetIntegration = carthagoRepository.countByCreatedAtBetweenAndApresCTRAndStatutChequeAndCodeValeur(
                start, end, Carthago.StatutCheque.REJETE, Carthago.CodeValeur.CHEQUE);

            data.add(new DataRowDTO("Nbr Chèque INTRA", String.valueOf(nbChequeINTRA == null ? 0 : nbChequeINTRA), null, "success"));
            data.add(new DataRowDTO("Nbr Chèque INTER", String.valueOf(nbChequeINTER == null ? 0 : nbChequeINTER), null, "success"));
            data.add(new DataRowDTO("Nbr Chèque Total", String.valueOf(nbChequeTotal), null, "success"));
            data.add(new DataRowDTO("Rejet d'intégration", String.valueOf(rejetIntegration == null ? 0 : rejetIntegration), null, 
                rejetIntegration != null && rejetIntegration > 0 ? "danger" : "success"));

        } catch (Exception e) {
            System.err.println("❌ Erreur buildAmplitudeCard: " + e.getMessage());
            data.add(new DataRowDTO("Erreur", "Données indisponibles", null, "danger"));
        }
        card.setData(data);
        return card;
    }

    /**
     * Statistiques globales
     */
    private List<StatCardDTO> buildGlobalStats(LocalDateTime start, LocalDateTime end) {
        List<StatCardDTO> stats = new ArrayList<>();

        try {
            // Alertes détectées
            Long rejets = carthagoRepository.countByCreatedAtBetweenAndApresCTRAndStatutChequeAndCodeValeur(
                start, end, Carthago.StatutCheque.REJETE, Carthago.CodeValeur.CHEQUE);
            Long evNonParvenus = carthagoRepository.countByCreatedAtBetweenAndFichierEnvAndCodeValeur(
                start, end, false, Carthago.CodeValeur.CHEQUE);
            int totalAlertes = (rejets == null ? 0 : rejets.intValue()) + 
                              (evNonParvenus == null ? 0 : evNonParvenus.intValue());

            // Statut global
            String statutGlobal = totalAlertes == 0 ? "OK" : "NOT OK";
            String statutStatus = totalAlertes == 0 ? "success" : "danger";

            // Session du jour
            String sessionDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            stats.add(new StatCardDTO(String.valueOf(totalAlertes), "Alertes détectées", null, 
                totalAlertes > 0 ? "warning" : "success"));
            stats.add(new StatCardDTO(statutGlobal, "Statut global", null, statutStatus));
            stats.add(new StatCardDTO(sessionDate, "Session du jour", null, "info"));

        } catch (Exception e) {
            System.err.println("❌ Erreur buildGlobalStats: " + e.getMessage());
            stats.add(new StatCardDTO("ERROR", "Erreur calcul stats", null, "danger"));
        }

        return stats;
    }
}
