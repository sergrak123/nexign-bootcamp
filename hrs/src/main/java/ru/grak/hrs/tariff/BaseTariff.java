package ru.grak.hrs.tariff;

import ru.grak.common.dto.CallDataRecordPlusDto;
import ru.grak.hrs.repository.CallCostRepository;
import ru.grak.hrs.repository.TariffRepository;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Интерфейс для реализации тарификации вызовов.
 */
public interface BaseTariff {

    /**
     * Рассчитывает стоимость вызова в соответствии с тарифом.
     *
     * @param cdrPlus            Расширенные данные о вызове.
     * @param tariffRepository   Репозиторий тарифов (кэшируется).
     * @param callCostRepository Репозиторий стоимости вызовов (внутри и вне сети) (кэшируется).
     * @return Стоимость вызова.
     */
    BigDecimal calculateCallCost(CallDataRecordPlusDto cdrPlus,
                                 TariffRepository tariffRepository,
                                 CallCostRepository callCostRepository);

    /**
     * Рассчитывает длительность вызова в минутах, округляя в большую
     * сторону.
     *
     * @param callDataRecordPlusDto Данные о вызове.
     * @return Длительность вызова в минутах.
     */
    default long calculateCallDuration(CallDataRecordPlusDto callDataRecordPlusDto) {

        var duration = Duration.ofSeconds(callDataRecordPlusDto.getDateTimeEndCall()
                - callDataRecordPlusDto.getDateTimeStartCall());

        if (duration.toSeconds() % 60 > 0) {
            duration = duration.truncatedTo(ChronoUnit.MINUTES).plusMinutes(1);
        }

        return duration.toMinutes();
    }

}
