package ru.grak.hrs.tariff;

import org.springframework.stereotype.Component;
import ru.grak.hrs.exception.TariffConversionException;
import ru.grak.hrs.tariff.impl.ClassicTariff;
import ru.grak.hrs.tariff.impl.MonthlyTariff;

import java.util.Map;

@Component
public final class TariffFactory {

    private TariffFactory() {
    }

    public static BaseTariff createTariff(String typeTariff, Map<String, Integer> usedMinutes) {

        if (typeTariff.equals("11")) {
            return new ClassicTariff();
        }
        if (typeTariff.equals("12")) {
            return new MonthlyTariff(usedMinutes);
        }

        throw new TariffConversionException("Tariff type conversion error");
    }
}
