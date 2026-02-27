package com.golfoutingapp.billing;

import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class StripeClientImpl implements StripeClient {
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public CheckoutData createPaymentCheckoutSession(Long eventId, Long registrationId, long amountCents, String currency, String attendeeEmail) {
        try {
            var params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(baseUrl + "/success")
                    .setCancelUrl(baseUrl + "/cancel")
                    .setCustomerEmail(attendeeEmail)
                    .putMetadata("registrationId", String.valueOf(registrationId))
                    .putMetadata("eventId", String.valueOf(eventId))
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency(currency)
                                    .setUnitAmount(amountCents)
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder().setName("Golf outing registration").build())
                                    .build())
                            .build())
                    .build();
            var session = com.stripe.model.checkout.Session.create(params);
            return new CheckoutData(session.getId(), session.getUrl());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create checkout session", e);
        }
    }

    @Override
    public CheckoutData createSubscriptionCheckoutSession(Long courseId, String customerEmail) {
        try {
            var params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(baseUrl + "/billing/success")
                    .setCancelUrl(baseUrl + "/billing/cancel")
                    .setCustomerEmail(customerEmail)
                    .putMetadata("courseId", String.valueOf(courseId))
                    .addLineItem(SessionCreateParams.LineItem.builder().setQuantity(1L)
                            .setPrice("${STRIPE_SUBSCRIPTION_PRICE_ID}")
                            .build())
                    .build();
            var session = com.stripe.model.checkout.Session.create(params);
            return new CheckoutData(session.getId(), session.getUrl());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create subscription checkout", e);
        }
    }
}
