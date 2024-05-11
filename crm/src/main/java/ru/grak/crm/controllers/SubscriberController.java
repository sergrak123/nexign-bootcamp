package ru.grak.crm.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.grak.crm.dto.PaymentDto;
import ru.grak.crm.entity.Client;
import ru.grak.crm.service.SubscriberService;

@RestController
@RequestMapping("/api/subscriber")
@RequiredArgsConstructor
@Tag(name = "Subscriber Controller", description = "APIs for managing subscriber's account")
public class SubscriberController {

    private final SubscriberService subscriberService;

    @PatchMapping("/pay")
    @PreAuthorize("hasAnyAuthority('USER')")
    public Client pay(@Valid @RequestBody PaymentDto payment) {
        return subscriberService.pay(payment);
    }

}
