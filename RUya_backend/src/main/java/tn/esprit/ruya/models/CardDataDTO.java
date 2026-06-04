package tn.esprit.ruya.models;

import lombok.Data;

import java.util.List;

@Data
public class CardDataDTO {
    private String title;
    private String icon;
    private String type; // 'primary', 'success', 'warning', 'default'
    private List<DataRowDTO> data;
}