package ru.grak.cdr.service.db;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.grak.cdr.entity.Abonent;
import ru.grak.cdr.repository.AbonentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AbonentService {

    private final AbonentRepository abonentRepository;

    /**
     * Получает список всех абонентов.
     *
     * @return Список всех абонентов.
     */
    public List<Abonent> getAbonentsList() {
        return abonentRepository.findAll();
    }

    /**
     * Находит абонента по номеру телефона. Необходимо для генерации
     * зеркальной записи. Запросы дополнительно кэшируются
     * для оптимизации при повторном запросе.
     *
     * @param msisdn Номер телефона абонента.
     * @return Абонент с указанным номером телефона, если найден.
     */
    @Cacheable("abonents")
    public Abonent findByPhoneNumber(String msisdn) {
        return abonentRepository.findByPhoneNumber(msisdn);
    }

}
