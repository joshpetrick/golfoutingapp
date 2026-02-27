package com.golfoutingapp.event;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OutingEventRepository extends JpaRepository<OutingEvent, Long> {
    Optional<OutingEvent> findByIdAndCourseId(Long eventId, Long courseId);
    List<OutingEvent> findByStatusAndStartTimeBetween(EventStatus status, Instant from, Instant to);
    List<OutingEvent> findByStatus(EventStatus status);
}
