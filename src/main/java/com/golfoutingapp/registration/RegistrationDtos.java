package com.golfoutingapp.registration;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RegistrationDtos {
    public record CreateRegistrationRequest(@NotBlank String attendeeName, @Email String attendeeEmail, @NotNull @Min(1) Integer spots) {}
    public record RegistrationCheckoutResponse(Long registrationId, String checkoutSessionId, String checkoutUrl) {}
}
