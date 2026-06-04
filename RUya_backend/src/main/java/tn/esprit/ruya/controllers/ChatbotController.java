package tn.esprit.ruya.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ruya.services.ChatbotService;
import tn.esprit.ruya.models.ChatRequest;
import tn.esprit.ruya.models.ChatResponse;

@RestController
@AllArgsConstructor
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "http://localhost:4200")
public class ChatbotController {

    private ChatbotService chatbotService;

    @PostMapping("/ask")
    public ResponseEntity<ChatResponse> askQuestion(@RequestBody ChatRequest request) {
        try {
            ChatResponse response = chatbotService.processQuestion(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("❌ Erreur chatbot: " + e.getMessage());
            ChatResponse errorResponse = new ChatResponse(
                "Désolé, je n'ai pas pu traiter votre question. Veuillez réessayer.",
                "error",
                null
            );
            return ResponseEntity.ok(errorResponse);
        }
    }

    @GetMapping("/suggestions")
    public ResponseEntity<String[]> getSuggestions() {
        String[] suggestions = {
            "Fichiers ajoutés aujourd'hui ?",
            "Montant total aujourd'hui ?",
            "Montant total ce mois ?",
            "Temps chèques ?",
            "Temps virements ?",
            "Temps effets ?",
            "Temps prélèvements ?",
            "Adresse siège ?"
        };
        return ResponseEntity.ok(suggestions);
    }
}
