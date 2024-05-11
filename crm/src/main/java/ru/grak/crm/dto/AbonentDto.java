package ru.grak.crm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.grak.common.enums.TypeTariff;
import ru.grak.crm.entity.Client;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbonentDto {

    @Pattern(regexp = "^(7|8)\\d{10}$")
    private String msisdn;

    @NotBlank
    private String tariffId;

    @Positive
    private BigDecimal money = BigDecimal.valueOf(100);

    public Client toEntity() {
        return Client.builder()
                .phoneNumber(this.getMsisdn())
                .tariff(TypeTariff.fromNumericValueOfType(this.getTariffId()))
                .balance(this.getMoney())
                .build();
    }
}
