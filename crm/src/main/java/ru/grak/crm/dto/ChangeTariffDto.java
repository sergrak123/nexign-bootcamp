package ru.grak.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeTariffDto {

    @Pattern(regexp = "^(7|8)\\d{10}$")
    private String msisdn;

    @NotBlank
    private String tariffId;
}
