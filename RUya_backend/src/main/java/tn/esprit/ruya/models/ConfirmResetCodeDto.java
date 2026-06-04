package tn.esprit.ruya.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmResetCodeDto {
    private String email;
    private String resetCode;
    private String newPassword;
}