package ru.grak.brt.service.automation;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.grak.brt.entity.Client;
import ru.grak.brt.repository.ClientRepository;
import ru.grak.brt.service.billing.BalanceService;
import ru.grak.common.enums.TypeTariff;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AutoUpdatingDataService {

    private final ClientRepository clientRepository;
    private final BalanceService balanceService;

    @Value("${brt.deposit.max}")
    private int maxAutoDeposit;

    @Value("${brt.random.clients.amount}")
    private int maxClientsForUpdatingTariff;

    public void autoChangeBalanceAndTariff() {
        List<Client> clients = clientRepository.findAll();
        refillClientsBalance(clients);
        changeClientsTariff(clients);
    }

    private void refillClientsBalance(List<Client> clients) {
        for (Client client : clients) {
            BigDecimal deposit = BigDecimal.valueOf(
                    ThreadLocalRandom.current().nextInt(maxAutoDeposit));

            balanceService.refillBalance(client, deposit);
        }
    }

    private void changeClientsTariff(List<Client> clients) {
        int amountClientForChanging = ThreadLocalRandom.current()
                .nextInt(maxClientsForUpdatingTariff) + 1;

        for (int i = 0; i < amountClientForChanging; i++) {
            var client = clients.get(
                    ThreadLocalRandom.current().nextInt(clients.size()));

            var currentTariff = client.getTariff();
            var updatedTariff = getModifiedTariff(currentTariff);
            client.setTariff(updatedTariff);

            clientRepository.save(client);
        }
    }

    //жесткая привязка
    private TypeTariff getModifiedTariff(TypeTariff currentTariff) {
        return currentTariff.equals(TypeTariff.PER_MINUTE)
                ? TypeTariff.MONTHLY
                : TypeTariff.PER_MINUTE;
    }

}
