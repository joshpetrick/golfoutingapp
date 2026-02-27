package com.golfoutingapp.registration;

import com.golfoutingapp.billing.StripeClient;
import com.golfoutingapp.common.ApiException;
import com.golfoutingapp.event.EventStatus;
import com.golfoutingapp.event.OutingEvent;
import com.golfoutingapp.event.OutingEventRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final PaymentRepository paymentRepository;
    private final OutingEventRepository eventRepository;
    private final StripeClient stripeClient;

    public RegistrationService(RegistrationRepository registrationRepository, PaymentRepository paymentRepository, OutingEventRepository eventRepository, StripeClient stripeClient) {
        this.registrationRepository = registrationRepository;
        this.paymentRepository = paymentRepository;
        this.eventRepository = eventRepository;
        this.stripeClient = stripeClient;
    }

    @Transactional
    public RegistrationDtos.RegistrationCheckoutResponse create(Long eventId, RegistrationDtos.CreateRegistrationRequest req) {
        OutingEvent event = eventRepository.findById(eventId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event not found"));
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Event is not open for registrations");
        }

        Registration reg = new Registration();
        reg.setEvent(event);
        reg.setAttendeeName(req.attendeeName());
        reg.setAttendeeEmail(req.attendeeEmail());
        reg.setSpots(req.spots());
        reg.setStatus(RegistrationStatus.PENDING);
        reg = registrationRepository.save(reg);

        int amount = req.spots() * event.getPriceCents();
        StripeClient.CheckoutData checkout = stripeClient.createPaymentCheckoutSession(eventId, reg.getId(), amount, event.getCurrency(), req.attendeeEmail());

        Payment payment = new Payment();
        payment.setRegistration(reg);
        payment.setAmountCents(amount);
        payment.setCurrency(event.getCurrency());
        payment.setStripeCheckoutSessionId(checkout.sessionId());
        payment.setStatus(PaymentStatus.CREATED);
        paymentRepository.save(payment);

        return new RegistrationDtos.RegistrationCheckoutResponse(reg.getId(), checkout.sessionId(), checkout.url());
    }

    public List<Registration> listForEvent(Long eventId) {
        return registrationRepository.findByEventId(eventId);
    }

    @Transactional
    public void confirmFromCheckoutSession(String sessionId) {
        Payment payment = paymentRepository.findByStripeCheckoutSessionId(sessionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Payment not found"));
        Registration registration = payment.getRegistration();
        OutingEvent event = registration.getEvent();

        int confirmedSpots = registrationRepository.confirmedSpots(event.getId());
        if (confirmedSpots + registration.getSpots() > event.getCapacity()) {
            payment.setStatus(PaymentStatus.REFUNDED);
            registration.setStatus(RegistrationStatus.CANCELED);
        } else {
            payment.setStatus(PaymentStatus.PAID);
            registration.setStatus(RegistrationStatus.CONFIRMED);
        }
        paymentRepository.save(payment);
        registrationRepository.save(registration);
    }
}
