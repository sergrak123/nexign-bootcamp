package ru.grak.hrs.tariff;

import org.springframework.stereotype.Component;
import ru.grak.hrs.exception.TariffConversionException;
import ru.grak.hrs.tariff.impl.ClassicTariff;
import ru.grak.hrs.tariff.impl.MonthlyTariff;

import java.util.Map;

/**
 * Фабрика для создания тарифов.
 */
@Component
public final class TariffFactory {

    private TariffFactory() {
    }

    /**
     * Создает экземпляр тарифа в зависимости от типа тарифа.
     *
     * @param typeTariff  Тип тарифа.
     * @param usedMinutes Использованные минуты для помесячного тарифа.
     * @return Экземпляр тарифа.
     * @throws TariffConversionException Если произошла ошибка преобразования типа тарифа.
     */
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
