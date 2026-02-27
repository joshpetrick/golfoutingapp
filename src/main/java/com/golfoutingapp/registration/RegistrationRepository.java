package com.golfoutingapp.registration;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    List<Registration> findByEventId(Long eventId);

    @Query("select coalesce(sum(r.spots),0) from Registration r where r.event.id = :eventId and r.status = 'CONFIRMED'")
    int confirmedSpots(Long eventId);
}
