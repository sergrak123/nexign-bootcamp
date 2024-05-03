package ru.grak.crm.dto;

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

    private String msisdn;
    private String tariffId;
    private BigDecimal money = BigDecimal.valueOf(100);

    public Client toEntity() {
        return Client.builder()
                .phoneNumber(this.getMsisdn())
                .tariff(TypeTariff.fromNumericValueOfType(this.getTariffId()))
                .balance(this.getMoney())
                .build();
    }
}
