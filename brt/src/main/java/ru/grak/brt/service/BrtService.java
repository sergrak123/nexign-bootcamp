package ru.grak.brt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.grak.brt.service.automation.AutoUpdatingDataService;
import ru.grak.brt.service.billing.AuthorizationService;
import ru.grak.brt.service.billing.BalanceService;
import ru.grak.brt.service.billing.CdrPlusService;
import ru.grak.common.dto.CallDataRecordDto;
import ru.grak.common.dto.CallDataRecordPlusDto;
import ru.grak.common.dto.InvoiceDto;
import ru.grak.common.enums.TypeCall;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrtService {

    private final AuthorizationService auth;
    private final CdrPlusService cdrPlusService;
    private final AutoUpdatingDataService autoUpdatingDataService;
    private final BalanceService balanceService;

    private final KafkaTemplate<String, CallDataRecordPlusDto> kafkaTemplate;

    private int currentMonth = 1;

    //TODO transactional

    @KafkaListener(topics = "brt", groupId = "brt-topic-default", containerFactory = "kafkaListenerContainerFactory")
    public void processingAndSendingCallData(String data) {

        List<CallDataRecordDto> cdr = parseCallDataFromReceivedData(data);
        log.info("Received: " + cdr);

        for (CallDataRecordDto callDataRecord : cdr) {
            int callMonth = extractMonthFromCallData(callDataRecord);

            if (callMonth > currentMonth) {
                currentMonth = callMonth;
                autoUpdatingDataService.autoChangeBalanceAndTariff();
            }

            if (auth.isAuthorizedMsisdn(callDataRecord.getMsisdnFirst())) {
                log.info("Auth:" + callDataRecord);
                CallDataRecordPlusDto cdrPlus = cdrPlusService.createCdrPlus(callDataRecord);
                kafkaTemplate.send("hrs", cdrPlus);
            }
        }
    }

    @KafkaListener(topics = "hrs-reply", groupId = "hrs-topic-reply-default", containerFactory =
            "costDataKafkaListenerContainerFactory")
    public void processingCostData(InvoiceDto invoiceData) {
        log.info(invoiceData.toString());
        balanceService.decreaseBalance(invoiceData.getMsisdn(), invoiceData.getCost());
    }

    private List<CallDataRecordDto> parseCallDataFromReceivedData(String data) {

        List<CallDataRecordDto> callDataRecordList = new ArrayList<>();
        var callDataRaws = data.split("\n");

        for (String raw : callDataRaws) {
            var cdr = raw.split(", ");

            callDataRecordList.add(
                    CallDataRecordDto
                            .builder()
                            .typeCall(TypeCall.fromNumericValueOfType(cdr[0]))
                            .msisdnFirst(cdr[1])
                            .msisdnSecond(cdr[2])
                            .dateTimeStartCall(Long.parseLong(cdr[3]))
                            .dateTimeEndCall(Long.parseLong(cdr[4]))
                            .build()
            );
        }

        return callDataRecordList;
    }

    private int extractMonthFromCallData(CallDataRecordDto callDataRecord) {
        var dateTimeStartCall = callDataRecord.getDateTimeStartCall();

        return LocalDate.
                ofInstant(Instant.ofEpochSecond(dateTimeStartCall), ZoneOffset.UTC)
                .getMonthValue();
    }

    //test - необходимо для возожности подмены данных генератора реальными файлами
    //@EventListener(ApplicationReadyEvent.class)
    public void filesProcessingAndSendingCallData() throws IOException {

        String CDR_FOLDER_PATH = "brt/data/";

        List<Path> cdrFiles = Files.list(Path.of(CDR_FOLDER_PATH)).toList();

        for (Path cdrFile : cdrFiles) {
            var cdrFilePath = Paths.get(CDR_FOLDER_PATH + cdrFile.getFileName());

            var data = Files.lines(cdrFilePath)
                    .collect(Collectors.joining("\n"));

            processingAndSendingCallData(data);
        }

    }

    //test
    public List<CallDataRecordDto> getAuthorizedCallDataRecord() throws IOException {
        String cdrFileName = "brt/data/cdr.txt";

        var data = Files.lines(Paths.get(cdrFileName))
                .collect(Collectors.joining("\n"));

        List<CallDataRecordDto> cdr = parseCallDataFromReceivedData(data);
        List<CallDataRecordDto> authorizedCdr = new ArrayList<>();

        for (CallDataRecordDto callDataRecord : cdr) {

            if (auth.isAuthorizedMsisdn(callDataRecord.getMsisdnFirst())) {
                authorizedCdr.add(callDataRecord);
            }
        }
        return authorizedCdr;
    }

}
