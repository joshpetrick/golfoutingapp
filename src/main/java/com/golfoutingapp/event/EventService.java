package com.golfoutingapp.event;

import com.golfoutingapp.billing.SubscriptionRepository;
import com.golfoutingapp.billing.SubscriptionStatus;
import com.golfoutingapp.common.ApiException;
import com.golfoutingapp.course.CourseAccessService;
import com.golfoutingapp.course.CourseRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class EventService {
    private final OutingEventRepository eventRepository;
    private final CourseRepository courseRepository;
    private final CourseAccessService accessService;
    private final SubscriptionRepository subscriptionRepository;

    public EventService(OutingEventRepository eventRepository, CourseRepository courseRepository, CourseAccessService accessService, SubscriptionRepository subscriptionRepository) {
        this.eventRepository = eventRepository;
        this.courseRepository = courseRepository;
        this.accessService = accessService;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional
    public EventDtos.EventResponse create(Long courseId, Long userId, EventDtos.UpsertEventRequest req) {
        accessService.requireMembership(courseId, userId);
        var course = courseRepository.findById(courseId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Course not found"));
        OutingEvent event = new OutingEvent();
        event.setCourse(course);
        apply(req, event);
        event.setStatus(EventStatus.DRAFT);
        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public EventDtos.EventResponse update(Long courseId, Long eventId, Long userId, EventDtos.UpsertEventRequest req) {
        accessService.requireMembership(courseId, userId);
        OutingEvent event = eventRepository.findByIdAndCourseId(eventId, courseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event not found"));
        apply(req, event);
        return toResponse(eventRepository.save(event));
    }

    @Transactional
    public EventDtos.EventResponse publish(Long courseId, Long eventId, Long userId) {
        accessService.requireMembership(courseId, userId);
        var sub = subscriptionRepository.findByCourseId(courseId)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "No subscription configured"));
        if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Subscription must be ACTIVE to publish");
        }
        OutingEvent event = eventRepository.findByIdAndCourseId(eventId, courseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event not found"));
        event.setStatus(EventStatus.PUBLISHED);
        return toResponse(eventRepository.save(event));
    }

    public List<EventDtos.EventResponse> listPublished(Instant from, Instant to) {
        List<OutingEvent> events = from != null && to != null
                ? eventRepository.findByStatusAndStartTimeBetween(EventStatus.PUBLISHED, from, to)
                : eventRepository.findByStatus(EventStatus.PUBLISHED);
        return events.stream().map(this::toResponse).toList();
    }

    public EventDtos.EventResponse get(Long eventId) {
        OutingEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event not found"));
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Event not published");
        }
        return toResponse(event);
    }

    private void apply(EventDtos.UpsertEventRequest req, OutingEvent event) {
        event.setTitle(req.title());
        event.setDescription(req.description());
        event.setStartTime(req.startTime());
        event.setSignupDeadline(req.signupDeadline());
        event.setCapacity(req.capacity());
        event.setPriceCents(req.priceCents());
        event.setCurrency(req.currency().toLowerCase());
    }

    private EventDtos.EventResponse toResponse(OutingEvent e) {
        return new EventDtos.EventResponse(e.getId(), e.getCourse().getId(), e.getTitle(), e.getDescription(), e.getStartTime(), e.getSignupDeadline(), e.getCapacity(), e.getPriceCents(), e.getCurrency(), e.getStatus());
    }
}
