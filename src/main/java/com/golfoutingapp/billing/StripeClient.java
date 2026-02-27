package com.golfoutingapp.billing;

public interface StripeClient {
    CheckoutData createPaymentCheckoutSession(Long eventId, Long registrationId, long amountCents, String currency, String attendeeEmail);
    CheckoutData createSubscriptionCheckoutSession(Long courseId, String customerEmail);

    record CheckoutData(String sessionId, String url) {}
}
