package tn.esprit.ruya.models;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

public class StatCardDTO {
    private String number;
    private String label;
    private String amount;
    private String status;

    public StatCardDTO() {}

    public StatCardDTO(String number, String label, String amount, String status) {
        this.number = number;
        this.label = label;
        this.amount = amount;
        this.status = status;
    }

    public StatCardDTO(String number, String label) {
        this(number, label, null, null);
    }

    // Getters et Setters
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}