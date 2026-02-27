package com.golfoutingapp.event;

import com.golfoutingapp.config.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api")
public class EventController {
    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/courses/{courseId}/events")
    public EventDtos.EventResponse create(@PathVariable Long courseId, @RequestBody @Valid EventDtos.UpsertEventRequest req, Authentication auth) {
        return eventService.create(courseId, ((CurrentUser) auth.getPrincipal()).id(), req);
    }

    @PutMapping("/courses/{courseId}/events/{eventId}")
    public EventDtos.EventResponse update(@PathVariable Long courseId, @PathVariable Long eventId, @RequestBody @Valid EventDtos.UpsertEventRequest req, Authentication auth) {
        return eventService.update(courseId, eventId, ((CurrentUser) auth.getPrincipal()).id(), req);
    }

    @PostMapping("/courses/{courseId}/events/{eventId}/publish")
    public EventDtos.EventResponse publish(@PathVariable Long courseId, @PathVariable Long eventId, Authentication auth) {
        return eventService.publish(courseId, eventId, ((CurrentUser) auth.getPrincipal()).id());
    }

    @GetMapping("/events")
    public List<EventDtos.EventResponse> list(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return eventService.listPublished(from, to);
    }

    @GetMapping("/events/{eventId}")
    public EventDtos.EventResponse get(@PathVariable Long eventId) {
        return eventService.get(eventId);
    }
}
