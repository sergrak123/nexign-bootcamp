package ru.grak.hrs.tariff.impl;

import ru.grak.common.dto.CallDataRecordPlusDto;
import ru.grak.common.enums.TypeCall;
import ru.grak.hrs.entity.Tariff;
import ru.grak.hrs.repository.CallCostRepository;
import ru.grak.hrs.repository.TariffRepository;
import ru.grak.hrs.tariff.BaseTariff;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Ежемесячный тариф для тарификации вызовов.
 */
public class MonthlyTariff implements BaseTariff {

    private final Map<String, Integer> usedMinutes;

    public MonthlyTariff(Map<String, Integer> usedMinutes) {
        this.usedMinutes = usedMinutes;
    }

    /**
     * Рассчитывает стоимость вызова в соответствии с ежемесячным тарифом
     * и количеством использованных минут в месяце (usedMinutes).
     *
     * @param cdrPlus            Расширенные данные о вызове.
     * @param tariffRepository   Репозиторий тарифов (кэшируется).
     * @param callCostRepository Репозиторий стоимости вызовов (внутри и вне сети) (кэшируется).
     * @return Стоимость вызова.
     */
    @Override
    public BigDecimal calculateCallCost(CallDataRecordPlusDto cdrPlus,
                                        TariffRepository tariffRepository,
                                        CallCostRepository callCostRepository) {

        String msisdn = cdrPlus.getMsisdnFirst();

        var used = usedMinutes.getOrDefault(msisdn, 0);
        var callDuration = calculateCallDuration(cdrPlus);

        BigDecimal callCost = BigDecimal.ZERO;
        Tariff tariff = tariffRepository.findByTariff(cdrPlus.getTypeTariff());
        var limitMinutes = tariff.getBenefitMinutes();

        if (used + callDuration > limitMinutes) {

            var overLimit = used < limitMinutes
                    ? used + callDuration - limitMinutes
                    : callDuration;
            TypeCall typeCall = cdrPlus.getTypeCall();

            if (typeCall.equals(TypeCall.OUTGOING)) {

                var outCallCostType = tariff.getOutCallCostType();
                var callCostType = callCostRepository.findByCallCostType(outCallCostType);

                callCost = cdrPlus.isInternalCall()
                        ? callCostType.getInternalCost()
                        : callCostType.getExternalCost();

                callCost = callCost.multiply(BigDecimal.valueOf(overLimit));
            }
        }
        usedMinutes.put(msisdn, Math.toIntExact(used + callDuration));

        return callCost;
    }
}
