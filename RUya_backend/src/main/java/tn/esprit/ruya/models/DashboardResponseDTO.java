package tn.esprit.ruya.models;

import lombok.Data;
import java.util.List;

public class DashboardResponseDTO {
    private List<CardDataDTO> cardData;
    private List<StatCardDTO> globalStats;
    private String lastUpdate;
    private boolean connected;

    public DashboardResponseDTO() {}

    public DashboardResponseDTO(List<CardDataDTO> cardData, List<StatCardDTO> globalStats) {
        this.cardData = cardData;
        this.globalStats = globalStats;
        this.lastUpdate = java.time.LocalDateTime.now().toString();
        this.connected = true;
    }

    // Getters et Setters
    public List<CardDataDTO> getCardData() { return cardData; }
    public void setCardData(List<CardDataDTO> cardData) { this.cardData = cardData; }

    public List<StatCardDTO> getGlobalStats() { return globalStats; }
    public void setGlobalStats(List<StatCardDTO> globalStats) { this.globalStats = globalStats; }

    public String getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(String lastUpdate) { this.lastUpdate = lastUpdate; }

    public boolean isConnected() { return connected; }
    public void setConnected(boolean connected) { this.connected = connected; }
}