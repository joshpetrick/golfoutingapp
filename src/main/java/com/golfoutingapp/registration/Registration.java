package com.golfoutingapp.registration;

import com.golfoutingapp.event.OutingEvent;
import com.golfoutingapp.user.User;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "registrations")
public class Registration {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) private OutingEvent event;
    @ManyToOne private User user;
    @Column(nullable = false) private String attendeeName;
    @Column(nullable = false) private String attendeeEmail;
    @Column(nullable = false) private Integer spots;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false) private RegistrationStatus status = RegistrationStatus.PENDING;
    private Instant expiresAt;
    @Column(nullable = false) private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public OutingEvent getEvent() { return event; }
    public void setEvent(OutingEvent event) { this.event = event; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getAttendeeName() { return attendeeName; }
    public void setAttendeeName(String attendeeName) { this.attendeeName = attendeeName; }
    public String getAttendeeEmail() { return attendeeEmail; }
    public void setAttendeeEmail(String attendeeEmail) { this.attendeeEmail = attendeeEmail; }
    public Integer getSpots() { return spots; }
    public void setSpots(Integer spots) { this.spots = spots; }
    public RegistrationStatus getStatus() { return status; }
    public void setStatus(RegistrationStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
}
