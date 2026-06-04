package tn.esprit.ruya.models;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataRowDTO {
    private String label;
    private Object value; // peut être String ou Integer
    private String amount;
    private String status; // 'success', 'warning', 'danger'
}