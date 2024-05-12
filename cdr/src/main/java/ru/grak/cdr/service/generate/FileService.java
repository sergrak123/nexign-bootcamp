package ru.grak.cdr.service.generate;

import org.springframework.stereotype.Service;
import ru.grak.common.dto.CallDataRecordDto;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для работы с файлами.
 */
@Service
public class FileService {

    private static final String CDR_FOLDER_PATH = "cdr/data/";
    private static final String CDR_FILE_PREFIX = "cdr";
    private static final String CDR_FILE_EXTENSION = ".txt";

    /**
     * Сохраняет записи данных вызовов в файл.
     *
     * @param cdrList    Список записей данных вызова.
     * @param fileNumber Номер файла.
     * @throws IOException Если возникает ошибка ввода-вывода при записи в файл.
     */
    public void saveCallDataRecords(List<CallDataRecordDto> cdrList, int fileNumber) throws IOException {

        createDirectory(CDR_FOLDER_PATH);
        String cdrFileName = CDR_FOLDER_PATH + CDR_FILE_PREFIX + "_" + fileNumber + CDR_FILE_EXTENSION;

        try (PrintWriter writer = new PrintWriter(new FileWriter(cdrFileName))) {

            for (CallDataRecordDto callDataRecord : cdrList) {
                writer.println(cdrFormat(callDataRecord));
            }
        }
    }

    /**
     * Получает записи данных вызовов из файла в виде строки
     * для последующей отправки данных (через кафку) в BRT.
     *
     * @param fileNumber Номер файла.
     * @return Строка, содержащая записи данных вызовов.
     * @throws IOException Если возникает ошибка ввода-вывода при чтении из файла.
     */
    public String getCallDataRecords(int fileNumber) throws IOException {

        String cdrFileName = CDR_FOLDER_PATH + CDR_FILE_PREFIX + "_" + fileNumber + CDR_FILE_EXTENSION;

        return Files.lines(Paths.get(cdrFileName))
                .collect(Collectors.joining("\n"));
    }

    /**
     * Форматирует созданную запись звонка для записи в файл.
     *
     * @param dataRecord Запись для форматирования.
     * @return Строка, в отформатированном виде.
     */
    private static String cdrFormat(CallDataRecordDto dataRecord) {
        return dataRecord.getTypeCall().getNumericValueOfType() + ", "
                + dataRecord.getMsisdnFirst() + ", "
                + dataRecord.getMsisdnSecond() + ", "
                + dataRecord.getDateTimeStartCall() + ", "
                + dataRecord.getDateTimeEndCall();
    }

    private void createDirectory(String path) {
        new File(path).mkdirs();
    }
}
