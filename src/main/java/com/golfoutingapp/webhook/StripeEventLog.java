package com.golfoutingapp.webhook;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "stripe_events")
public class StripeEventLog {
    @Id
    private String id;
    private String type;
    private Instant createdAt = Instant.now();

    public StripeEventLog() {}
    public StripeEventLog(String id, String type) { this.id = id; this.type = type; }

    public String getId() { return id; }
    public String getType() { return type; }
}
