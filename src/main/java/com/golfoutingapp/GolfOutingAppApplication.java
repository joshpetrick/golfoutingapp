package com.golfoutingapp;

import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PostConstruct;

@SpringBootApplication
public class GolfOutingAppApplication {

    @Value("${stripe.secret-key:}")
    private String stripeSecretKey;

    public static void main(String[] args) {
        SpringApplication.run(GolfOutingAppApplication.class, args);
    }

    @PostConstruct
    void initStripe() {
        if (!stripeSecretKey.isBlank()) {
            Stripe.apiKey = stripeSecretKey;
        }
    }
}
