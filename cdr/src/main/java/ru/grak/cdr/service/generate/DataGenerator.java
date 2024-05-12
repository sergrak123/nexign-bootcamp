package ru.grak.cdr.service.generate;

import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.grak.cdr.entity.Abonent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DataGenerator {

    @Value("${cdr.unix-gap.min}")
    private int minUnixGap;

    @Value("${cdr.unix-gap.max}")
    private int maxUnixGap;

    /**
     * Генерирует случайную продолжительность вызова в секундах.
     *
     * @param maxCallDurationInMinutes Максимальная продолжительность вызова в минутах.
     * @return Случайная продолжительность вызова в секундах.
     */
    public long generateRandomCallDuration(int maxCallDurationInMinutes) {
        return ThreadLocalRandom.current().nextInt(maxCallDurationInMinutes * 60) + 1;
    }

    /**
     * Получает случайную пару номеров телефонов из списка абонентов, необходимых
     * для генерации звонка. При этом накладывается условие, что msisdn1 != msisdn2.
     * Реализацию с shuffle посчитал не оптимальным при многкратном вызове.
     *
     * @param abonents Список абонентов.
     * @return Пара различных номеров телефонов.
     */
    public Pair<String, String> getRandomPairOfMsisdn(List<Abonent> abonents) {

        String firstMsisdn = abonents.get(ThreadLocalRandom.current().nextInt(abonents.size()))
                .getPhoneNumber();
        String secondMsisdn = abonents.get(ThreadLocalRandom.current().nextInt(abonents.size()))
                .getPhoneNumber();

        while (secondMsisdn.equals(firstMsisdn)) {
            secondMsisdn = abonents.get(ThreadLocalRandom.current().nextInt(abonents.size()))
                    .getPhoneNumber();
        }

        return new Pair(firstMsisdn, secondMsisdn);
    }

    /**
     * Генерирует случайный временной промежуток в секундах для
     * создания промежутков между звонками.
     *
     * @return Случайный временной промежуток в секундах.
     */
    public long generateRandomGap() {
        return ThreadLocalRandom.current().nextInt(minUnixGap, maxUnixGap);
    }

    @Deprecated
    public static long convertToUnixTime(LocalDateTime dateTime) {
        return dateTime.toEpochSecond(ZoneOffset.UTC);
    }

    @Deprecated
    public static String generatePhoneNumber() {
        StringBuilder phoneNumber = new StringBuilder("7");
        for (int i = 0; i < 10; i++) {
            phoneNumber.append(ThreadLocalRandom.current().nextInt(10));
        }
        return phoneNumber.toString();
    }
}
