package com.golfoutingapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.golfoutingapp.auth.AuthDtos;
import com.golfoutingapp.billing.StripeClient;
import com.golfoutingapp.billing.Subscription;
import com.golfoutingapp.billing.SubscriptionRepository;
import com.golfoutingapp.billing.SubscriptionStatus;
import com.golfoutingapp.config.JwtService;
import com.golfoutingapp.course.*;
import com.golfoutingapp.event.*;
import com.golfoutingapp.registration.*;
import com.golfoutingapp.user.User;
import com.golfoutingapp.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApplicationIntegrationTests {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired CourseRepository courseRepository;
    @Autowired CourseMemberRepository courseMemberRepository;
    @Autowired SubscriptionRepository subscriptionRepository;
    @Autowired OutingEventRepository eventRepository;
    @Autowired RegistrationRepository registrationRepository;
    @Autowired PaymentRepository paymentRepository;
    @Autowired RegistrationService registrationService;
    @Autowired JwtService jwtService;

    @MockBean StripeClient stripeClient;

    @BeforeEach
    void resetMocks() {
        Mockito.when(stripeClient.createPaymentCheckoutSession(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(new StripeClient.CheckoutData("cs_test_123", "https://checkout.stripe.test/123"));
    }

    @Test
    void authRegisterAndLogin() throws Exception {
        var register = new AuthDtos.RegisterRequest("Alice", "alice@test.com", "password123");
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        var login = new AuthDtos.LoginRequest("alice@test.com", "password123");
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk());
    }

    @Test
    void publishBlockedWhenSubscriptionInactive() throws Exception {
        User user = new User();
        user.setName("Staff");
        user.setEmail("staff2@test.com");
        user.setPasswordHash(passwordEncoder.encode("password123"));
        userRepository.save(user);

        Course course = new Course();
        course.setName("Course 2");
        course.setTimezone("UTC");
        courseRepository.save(course);

        CourseMember member = new CourseMember();
        member.setCourse(course);
        member.setUser(user);
        member.setRole(CourseRole.COURSE_ADMIN);
        courseMemberRepository.save(member);

        Subscription sub = new Subscription();
        sub.setCourse(course);
        sub.setStatus(SubscriptionStatus.INCOMPLETE);
        subscriptionRepository.save(sub);

        OutingEvent event = new OutingEvent();
        event.setCourse(course);
        event.setTitle("Draft");
        event.setDescription("desc");
        event.setStartTime(Instant.now().plusSeconds(86400));
        event.setSignupDeadline(Instant.now().plusSeconds(3600));
        event.setCapacity(10);
        event.setPriceCents(5000);
        event.setCurrency("usd");
        eventRepository.save(event);

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        mockMvc.perform(post("/api/courses/" + course.getId() + "/events/" + event.getId() + "/publish")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrationCreationAndConfirmation() throws Exception {
        Course course = new Course();
        course.setName("Public Course");
        course.setTimezone("UTC");
        courseRepository.save(course);

        OutingEvent event = new OutingEvent();
        event.setCourse(course);
        event.setTitle("Public Event");
        event.setDescription("desc");
        event.setStartTime(Instant.now().plusSeconds(86400));
        event.setSignupDeadline(Instant.now().plusSeconds(3600));
        event.setCapacity(10);
        event.setPriceCents(1000);
        event.setCurrency("usd");
        event.setStatus(EventStatus.PUBLISHED);
        eventRepository.save(event);

        var req = new RegistrationDtos.CreateRegistrationRequest("Guest", "guest@test.com", 2);
        mockMvc.perform(post("/api/events/" + event.getId() + "/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        Payment payment = paymentRepository.findByStripeCheckoutSessionId("cs_test_123").orElseThrow();
        registrationService.confirmFromCheckoutSession(payment.getStripeCheckoutSessionId());

        Payment updatedPayment = paymentRepository.findById(payment.getId()).orElseThrow();
        Registration updatedReg = registrationRepository.findById(payment.getRegistration().getId()).orElseThrow();
        assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(updatedReg.getStatus()).isEqualTo(RegistrationStatus.CONFIRMED);
    }
}
