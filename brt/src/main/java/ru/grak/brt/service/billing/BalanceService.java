package ru.grak.brt.service.billing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.grak.brt.repository.ClientRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class BalanceService {

    //TODO native query
    private final ClientRepository clientRepository;

    /**
     * Уменьшает баланс клиента на указанную сумму.
     *
     * @param msisdn Номер телефона клиента.
     * @param cost   Сумма, которую необходимо вычесть из баланса клиента.
     */
    public void decreaseBalance(String msisdn, BigDecimal cost) {
        var client = clientRepository.findByPhoneNumber(msisdn);
        BigDecimal balance = client.getBalance();
        BigDecimal updatedBalance = balance.subtract(cost);
        client.setBalance(updatedBalance);

        clientRepository.save(client);
    }

    /**
     * Пополняет баланс клиента на указанную сумму.
     *
     * @param msisdn  Номер телефона клиента.
     * @param deposit Сумма, которую необходимо добавить к балансу клиента.
     */
    public void refillBalance(String msisdn, BigDecimal deposit) {
        var client = clientRepository.findByPhoneNumber(msisdn);
        BigDecimal balance = client.getBalance();
        BigDecimal updatedBalance = balance.add(deposit);
        client.setBalance(updatedBalance);

        clientRepository.save(client);
    }

    /**
     * Получает баланс клиента.
     *
     * @param msisdn Номер телефона клиента.
     * @return Баланс клиента.
     */
    public BigDecimal getBalance(String msisdn) {
        var client = clientRepository.findByPhoneNumber(msisdn);
        return client.getBalance();
    }

}
