package tn.esprit.ruya.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Dto {
    private Long fichierRemis;
    private Long fichierRecu;
    private Long fichierRepris;
    private Long fichierRendu;
    private Long fichierRecucheque;
    private Long fichierRejetcheque;
    private Long fichierRenducheque;
    private Long fichierRecuprlv;
    private Long fichierRejetprlv;
    private Long fichierRenduprlv;
    private Long fichierRecuvirment;
    private Long fichierRejetvirment;
    private Long fichierRenduvirment;
    private Long fichierRecueffet;
    private Long fichierRejeteffet;
    private Long fichierRendueffet;
    private Double totalMontant;
    private Integer totalNomber;
}