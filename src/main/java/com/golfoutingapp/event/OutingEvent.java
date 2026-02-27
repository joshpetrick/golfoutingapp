package com.golfoutingapp.event;

import com.golfoutingapp.course.Course;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "outing_events")
public class OutingEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false) private Course course;
    @Column(nullable = false) private String title;
    @Column(length = 2000) private String description;
    @Column(nullable = false) private Instant startTime;
    @Column(nullable = false) private Instant signupDeadline;
    @Column(nullable = false) private Integer capacity;
    @Column(nullable = false) private Integer priceCents;
    @Column(nullable = false) private String currency;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.DRAFT;

    public Long getId() { return id; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getSignupDeadline() { return signupDeadline; }
    public void setSignupDeadline(Instant signupDeadline) { this.signupDeadline = signupDeadline; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Integer getPriceCents() { return priceCents; }
    public void setPriceCents(Integer priceCents) { this.priceCents = priceCents; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }
}
