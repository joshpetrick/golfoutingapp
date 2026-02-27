package com.golfoutingapp.webhook;

import com.golfoutingapp.billing.BillingService;
import com.golfoutingapp.billing.SubscriptionStatus;
import com.golfoutingapp.registration.RegistrationService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.model.Subscription;
import com.stripe.net.Webhook;
import com.stripe.param.SubscriptionRetrieveParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
public class StripeWebhookController {
    private final StripeEventLogRepository stripeEventLogRepository;
    private final RegistrationService registrationService;
    private final BillingService billingService;
    @Value("${stripe.webhook-signing-secret:}")
    private String signingSecret;

    public StripeWebhookController(StripeEventLogRepository stripeEventLogRepository, RegistrationService registrationService, BillingService billingService) {
        this.stripeEventLogRepository = stripeEventLogRepository;
        this.registrationService = registrationService;
        this.billingService = billingService;
    }

    @PostMapping("/stripe")
    public ResponseEntity<Map<String, String>> handle(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) throws Exception {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, signingSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().body(Map.of("status", "invalid signature"));
        }

        if (stripeEventLogRepository.existsById(event.getId())) {
            return ResponseEntity.ok(Map.of("status", "duplicate ignored"));
        }
        stripeEventLogRepository.save(new StripeEventLog(event.getId(), event.getType()));

        switch (event.getType()) {
            case "checkout.session.completed" -> {
                Session session = (Session) event.getDataObjectDeserializer().getObject().orElseThrow();
                if ("payment".equals(session.getMode())) {
                    registrationService.confirmFromCheckoutSession(session.getId());
                }
            }
            case "customer.subscription.updated", "customer.subscription.created", "customer.subscription.deleted" -> {
                Subscription sub = (Subscription) event.getDataObjectDeserializer().getObject().orElseThrow();
                billingService.updateSubscription(
                        sub.getId(),
                        sub.getCustomer(),
                        mapStatus(sub.getStatus()),
                        sub.getCurrentPeriodEnd());
            }
            default -> {
            }
        }
        return ResponseEntity.ok(Map.of("status", "processed"));
    }

    private SubscriptionStatus mapStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "active" -> SubscriptionStatus.ACTIVE;
            case "trialing" -> SubscriptionStatus.TRIALING;
            case "past_due" -> SubscriptionStatus.PAST_DUE;
            case "canceled", "unpaid" -> SubscriptionStatus.CANCELED;
            default -> SubscriptionStatus.INCOMPLETE;
        };
    }
}
