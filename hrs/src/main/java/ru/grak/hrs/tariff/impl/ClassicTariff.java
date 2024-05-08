package ru.grak.hrs.tariff.impl;

import ru.grak.common.dto.CallDataRecordPlusDto;
import ru.grak.common.enums.TypeCall;
import ru.grak.hrs.entity.Tariff;
import ru.grak.hrs.repository.CallCostRepository;
import ru.grak.hrs.repository.TariffRepository;
import ru.grak.hrs.tariff.BaseTariff;

import java.math.BigDecimal;

public class ClassicTariff implements BaseTariff {

    @Override
    public BigDecimal calculateCallCost(CallDataRecordPlusDto cdrPlus,
                                        TariffRepository tariffRepository,
                                        CallCostRepository callCostRepository) {

        var callDuration = calculateCallDuration(cdrPlus);

        TypeCall typeCall = cdrPlus.getTypeCall();
        Tariff typeTariff = tariffRepository.findByTariff(cdrPlus.getTypeTariff());
        BigDecimal callCost = BigDecimal.ZERO;

        if (typeCall.equals(TypeCall.OUTGOING)) {

            var outCallCostType = typeTariff.getOutCallCostType();
            var callCostType = callCostRepository.findByCallCostType(outCallCostType);

            callCost = cdrPlus.isInternalCall()
                    ? callCostType.getInternalCost()
                    : callCostType.getExternalCost();

            callCost = callCost.multiply(BigDecimal.valueOf(callDuration));
        }

        return callCost;
    }
}
