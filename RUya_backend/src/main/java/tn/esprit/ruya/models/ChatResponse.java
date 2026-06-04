package tn.esprit.ruya.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String response;
    private String type; // "success", "error", "info"
    private Object data; // Données supplémentaires si nécessaire
}
