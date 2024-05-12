package ru.grak.hrs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.grak.common.dto.CallDataRecordPlusDto;
import ru.grak.common.dto.InvoiceDto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

/**
 * Сервис для обработки звонков и выставления счетов.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HrsService {

    private final KafkaTemplate<String, InvoiceDto> kafkaTemplate;
    private final TarifficationService tarifficationService;
    private final SubscriptionsFeeService subscriptionsFeeService;

    private final Map<String, Integer> usedMinutes = new HashMap<>();

    private int currentMonth = 1;

    /**
     * Метод обработки приходящих из BRT (топика кафки) расширенных данных о звонках
     * клиентов, тарификации звонков согласно их тарифам и количеству используемых
     * минут в конкретном месяце по каждому клиенту (при помесячном тарифе),
     * выставление счетов клиентам со стоимостью услуг для дальнейшей передачи их
     * обратно в BRT для списания стоимости.
     * <p>
     * При наступлении нового месяца происходит выставление счетов
     * со стоимостью абонентской платы (за предыдущий период) для всех
     * клиентов с помесячным тарифом.
     *
     * @param cdrPlus Расширенные данные о звонке.
     */
    @KafkaListener(topics = "hrs", groupId = "hrs-topic-default", containerFactory = "kafkaListenerContainerFactory")
    public void invoicing(CallDataRecordPlusDto cdrPlus) {
        log.info(cdrPlus.toString());

        int callMonth = extractMonthFromCallData(cdrPlus);

        if (callMonth > currentMonth) {
            currentMonth = callMonth;
            subscriptionsFeeService.subscriptionsFeeWithdrawal(usedMinutes);
        }

        var invoiceData = tarifficationService.createInvoice(cdrPlus, usedMinutes);
        kafkaTemplate.send("hrs-reply", invoiceData);

    }

    /**
     * Извлекает месяц из данных о звонке.
     *
     * @param cdrPlus Расширенные данные о звонке.
     * @return Месяц звонка.
     */
    private int extractMonthFromCallData(CallDataRecordPlusDto cdrPlus) {
        var dateTimeStartCall = cdrPlus.getDateTimeStartCall();

        return LocalDate.
                ofInstant(Instant.ofEpochSecond(dateTimeStartCall), ZoneOffset.UTC)
                .getMonthValue();
    }

}
