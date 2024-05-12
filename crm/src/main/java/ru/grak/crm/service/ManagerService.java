package ru.grak.crm.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import ru.grak.common.enums.TypeTariff;
import ru.grak.crm.dto.AbonentDto;
import ru.grak.crm.dto.ChangeTariffDto;
import ru.grak.crm.entity.Client;
import ru.grak.crm.exceptions.AbonentAlreadyExistException;
import ru.grak.crm.exceptions.ClientNotFoundException;
import ru.grak.crm.exceptions.TariffNotFoundException;
import ru.grak.crm.exceptions.UnchangedTariffException;
import ru.grak.crm.repository.ClientRepository;
import ru.grak.crm.repository.TariffRepository;

/**
 * Сервис для управления клиентами.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerService {

    private final TariffRepository tariffRepository;
    private final ClientRepository clientRepository;

    /**
     * Метод изменения тарифа для клиента.
     *
     * @param changeTariffDto информация о смене тарифа
     * @return Обновленный клиент
     * @throws ClientNotFoundException  если клиент не найден
     * @throws TariffNotFoundException  если тариф не найден
     * @throws UnchangedTariffException если тариф не был изменен
     */
    @Transactional
    @Modifying
    public Client changeTariff(ChangeTariffDto changeTariffDto) {

        var client = clientRepository.findByPhoneNumber(changeTariffDto.getMsisdn())
                .orElseThrow(() ->
                        new ClientNotFoundException("Client with msisdn:{0} not found", changeTariffDto.getMsisdn()));

        var tariff = tariffRepository.findById(TypeTariff.fromNumericValueOfType(changeTariffDto.getTariffId()))
                .orElseThrow(() ->
                        new TariffNotFoundException("Tariff with id:{0} not found", changeTariffDto.getTariffId()));

        if (client.getTariff().equals(tariff.getTariff()))
            throw new UnchangedTariffException("Tariff doesn't changed");

        client.setTariff(tariff.getTariff());
        return clientRepository.save(client);
    }

    /**
     * Метод создания абонента.
     *
     * @param abonentDto Информация об абоненте
     * @return Созданный клиент
     * @throws AbonentAlreadyExistException если абонент уже существует
     * @throws TariffNotFoundException      если тариф не найден
     */
    @Transactional
    public Client createAbonent(AbonentDto abonentDto) {

        if (clientRepository.existsByPhoneNumber(abonentDto.getMsisdn()))
            throw new AbonentAlreadyExistException("Abonent with number: {0} already exist", abonentDto.getMsisdn());

        if (!tariffRepository.existsByTariff(TypeTariff.fromNumericValueOfType(abonentDto.getTariffId())))
            throw new TariffNotFoundException("Tariff with id: {0} doesn't exist", abonentDto.getTariffId());

        return clientRepository.save(abonentDto.toEntity());
    }
}
