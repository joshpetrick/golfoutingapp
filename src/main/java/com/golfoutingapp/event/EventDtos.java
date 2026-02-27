package com.golfoutingapp.event;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class EventDtos {
    public record UpsertEventRequest(@NotBlank String title, String description, @NotNull Instant startTime,
                                     @NotNull Instant signupDeadline, @NotNull @Min(1) Integer capacity,
                                     @NotNull @Min(0) Integer priceCents, @NotBlank String currency) {}

    public record EventResponse(Long id, Long courseId, String title, String description, Instant startTime,
                                Instant signupDeadline, Integer capacity, Integer priceCents, String currency,
                                EventStatus status) {}
}
