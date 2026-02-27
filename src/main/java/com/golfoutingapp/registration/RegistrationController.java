package com.golfoutingapp.registration;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RegistrationController {
    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/events/{eventId}/registrations")
    public RegistrationDtos.RegistrationCheckoutResponse create(@PathVariable Long eventId,
                                                                @RequestBody @Valid RegistrationDtos.CreateRegistrationRequest req) {
        return registrationService.create(eventId, req);
    }
}
