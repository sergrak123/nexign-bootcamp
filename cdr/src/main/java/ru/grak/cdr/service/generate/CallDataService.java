package ru.grak.cdr.service.generate;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.grak.cdr.entity.Abonent;
import ru.grak.cdr.service.db.AbonentService;
import ru.grak.common.dto.CallDataRecordDto;
import ru.grak.common.enums.TypeCall;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Сервис для генерации данных вызовов.
 */
@Service
@RequiredArgsConstructor
public class CallDataService {

    private final DataGenerator dataGenerator;
    private final AbonentService abonentService;

    @Value("${cdr.generate.max-call-duration}")
    private int maxCallDuration;

    /**
     * Генерирует случайные данные вызова на основе начала времени звонка
     * и пары номеров телефонов, совершивших звонок.
     *
     * @param callStartDateTime Время начала вызова (Unix time).
     * @param msisdnPair        Пара номеров телефонов, которые совершили вызов.
     * @return Сгенерированные данные вызова (CallDataRecord).
     */
    public CallDataRecordDto generateRandomCallData(long callStartDateTime, Pair<String, String> msisdnPair) {

        TypeCall typeCall = ThreadLocalRandom.current().nextBoolean()
                ? TypeCall.OUTGOING
                : TypeCall.INCOMING;

        String firstMsisdn = msisdnPair.a;
        String secondMsisdn = msisdnPair.b;

        long callEndDateTime = callStartDateTime
                + dataGenerator.generateRandomCallDuration(maxCallDuration);

        return CallDataRecordDto.builder()
                .typeCall(typeCall)
                .msisdnFirst(firstMsisdn)
                .msisdnSecond(secondMsisdn)
                .dateTimeStartCall(callStartDateTime)
                .dateTimeEndCall(callEndDateTime)
                .build();
    }

    /**
     * Генерирует зеркальную запись для данных вызова, т.е меняет
     * тип вызова, меняет местами номера, время звонка остается прежним.
     *
     * @param record Запись с данными вызова.
     * @return Зеркальная запись данных вызова, если второй абонент - клиент "Ромашка".
     */
    public Optional<CallDataRecordDto> generateMirrorRecord(CallDataRecordDto record) {

        Abonent secondAbonent = abonentService.findByPhoneNumber(record.getMsisdnSecond());

        if (secondAbonent.isRomashkaClient()) {

            TypeCall mirrorTypeCall = record.getTypeCall().equals(TypeCall.OUTGOING)
                    ? TypeCall.INCOMING
                    : TypeCall.OUTGOING;

            return Optional.of(CallDataRecordDto.builder()
                    .typeCall(mirrorTypeCall)
                    .msisdnFirst(record.getMsisdnSecond())
                    .msisdnSecond(record.getMsisdnFirst())
                    .dateTimeStartCall(record.getDateTimeStartCall())
                    .dateTimeEndCall(record.getDateTimeEndCall())
                    .build());
        }

        return Optional.empty();
    }
}
