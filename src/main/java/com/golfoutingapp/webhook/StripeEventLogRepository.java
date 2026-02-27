package com.golfoutingapp.webhook;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StripeEventLogRepository extends JpaRepository<StripeEventLog, String> {}
