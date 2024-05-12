package ru.grak.cdr.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.grak.cdr.entity.Abonent;
import ru.grak.cdr.service.db.AbonentService;
import ru.grak.cdr.service.db.TransactionService;
import ru.grak.cdr.service.generate.CallDataService;
import ru.grak.cdr.service.generate.DataGenerator;
import ru.grak.cdr.service.generate.FileService;
import ru.grak.common.dto.CallDataRecordDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommutatorService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final CallDataService callDataService;
    private final DataGenerator dataGenerator;
    private final AbonentService abonentService;
    private final FileService fileService;
    private final TransactionService transactionService;

    @Value("${cdr.kafka.topic}")
    private String kafkaTopicName;

    @Value("${cdr.generate.file.capacity}")
    private int fileCapacity;

    @Value("${cdr.generate.date-time-start}")
    private long startUnixDateTime;

    @Value("${cdr.generate.data-time-end}")
    private long endUnixDateTime;

    private int recordsAmount;
    private int filesAmount;
    private final List<CallDataRecordDto> cdr = new ArrayList<>();

    /**
     * Генерирует записи данных вызовов при старте приложения, дополнительно
     * создавая зеркальую запись вызова при необходимости. При заполнении
     * буфера необходимым количеством записей (fileCapacity) сохраняет данные
     * в файл, добавляет их в базу данных о транзакциях и отправляет на обратку
     * в BRT (топик кафки).
     *
     * @throws IOException Если возникает ошибка ввода-вывода при сохранении данных.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void generateCallDataRecords() throws IOException {

        long currentUnixDateTime = startUnixDateTime;
        List<Abonent> abonents = abonentService.getAbonentsList();

        while (currentUnixDateTime <= endUnixDateTime) {

            var msisdnPair = dataGenerator.getRandomPairOfMsisdn(abonents);
            CallDataRecordDto callDataRecord = callDataService
                    .generateRandomCallData(currentUnixDateTime, msisdnPair);
            handleCallDataRecord(callDataRecord);

            var mirrorRecord = callDataService.generateMirrorRecord(callDataRecord);

            if (mirrorRecord.isPresent()) {
                handleCallDataRecord(mirrorRecord.get());
            }

            currentUnixDateTime += dataGenerator.generateRandomGap();
        }
    }

    /**
     * Добавляет запись в буфер. Обрабатывает данные буфера, если он заполнен.
     *
     * @param record Запись данных вызова.
     */
    private void handleCallDataRecord(CallDataRecordDto record) throws IOException {
        recordsAmount++;
        cdr.add(record);
        saveAndSendDataIfBatchReady();
    }

    /**
     * Сохраняет записи данных вызовов в файл, добавляет их в базу данных и отправляет на обратку
     * в BRT, если буфер готов к обработке.
     *
     * @throws IOException Если возникает ошибка ввода-вывода при сохранении или отправке данных.
     */
    private void saveAndSendDataIfBatchReady() throws IOException {
        if (isPrepared()) {
            filesAmount++;
            fileService.saveCallDataRecords(cdr, filesAmount);
            transactionService.saveTransactions(cdr);

            String fileData = fileService.getCallDataRecords(filesAmount);
            kafkaTemplate.send(kafkaTopicName, fileData);
            cdr.clear();
        }
    }

    /**
     * Проверяет, готов ли буфер к обработке данных вызовов (количество
     * записей кратно размерности файла).
     *
     * @return true, если буфер готов к обработке, иначе false.
     */
    private boolean isPrepared() {
        return recordsAmount % fileCapacity == 0;
    }

}
