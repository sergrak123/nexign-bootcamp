package ru.grak.hrs.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.grak.common.dto.InvoiceDto;
import ru.grak.common.enums.TypeTariff;
import ru.grak.hrs.repository.TariffRepository;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SubscriptionsFeeService {

    private final TariffRepository tariffRepository;
    private final KafkaTemplate<String, InvoiceDto> kafkaTemplate;

    /**
     * Рассчитывает абонентскую плату для каждого клиента с помесячным тарифом
     * и отправляет счета в BRT для списания с клиентов данной стоимости.
     *
     * @param usedMinutes Данные по каждому клиенту с помесячной оплатой.
     */
    public void subscriptionsFeeWithdrawal(Map<String, Integer> usedMinutes) {

        BigDecimal monthlySubscriptionsFee = tariffRepository
                .findByTariff(TypeTariff.MONTHLY)
                .getSubscriptionFee();

        for (String msisdn : usedMinutes.keySet()) {
            var monthlyInvoice = InvoiceDto.builder()
                    .msisdn(msisdn)
                    .cost(monthlySubscriptionsFee)
                    .build();

            kafkaTemplate.send("hrs-reply", monthlyInvoice);
        }

        usedMinutes.clear();
    }

}
