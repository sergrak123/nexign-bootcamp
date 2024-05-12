package ru.grak.cdr.service.db;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.grak.cdr.entity.Transaction;
import ru.grak.cdr.repository.TransactionRepository;
import ru.grak.common.dto.CallDataRecordDto;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    /**
     * Сохраняет транзакции клиентов о звонках в базу данных.
     *
     * @param callDataRecords Список записей данных вызовов для сохранения.
     */
    public void saveTransactions(List<CallDataRecordDto> callDataRecords) {

        for (CallDataRecordDto record : callDataRecords) {
            Transaction transaction = Transaction
                    .builder()
                    .typeCall(record.getTypeCall().getNumericValueOfType())
                    .msisdnFirst(record.getMsisdnFirst())
                    .msisdnSecond(record.getMsisdnSecond())
                    .dateTimeStartCall(record.getDateTimeStartCall())
                    .dateTimeEndCall(record.getDateTimeEndCall())
                    .build();

            transactionRepository.save(transaction);
        }
    }

}
