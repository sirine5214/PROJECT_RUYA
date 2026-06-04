package tn.esprit.ruya.controllers;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ruya.services.CarthagoService;
import tn.esprit.ruya.models.Carthago;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/carthago")
@AllArgsConstructor
public class CarthagoController {

    private final CarthagoService carthagoService;

    @GetMapping
    public ResponseEntity<List<Carthago>> getAll() {
        return ResponseEntity.ok(carthagoService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Carthago> getById(@PathVariable Long id) {
        Optional<Carthago> c = carthagoService.getById(id);
        return c.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Carthago> create(@RequestBody Carthago c) {
        return ResponseEntity.ok(carthagoService.create(c));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Carthago> update(@PathVariable Long id, @RequestBody Carthago c) {
        Carthago updated = carthagoService.update(id, c);
        if (updated != null)
            return ResponseEntity.ok(updated);
        else
            return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        carthagoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
