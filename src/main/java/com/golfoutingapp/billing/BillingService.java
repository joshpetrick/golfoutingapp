package com.golfoutingapp.billing;

import com.golfoutingapp.common.ApiException;
import com.golfoutingapp.course.CourseRepository;
import com.golfoutingapp.course.CourseAccessService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class BillingService {
    private final CourseRepository courseRepository;
    private final CourseAccessService accessService;
    private final SubscriptionRepository subscriptionRepository;
    private final StripeClient stripeClient;

    public BillingService(CourseRepository courseRepository, CourseAccessService accessService, SubscriptionRepository subscriptionRepository, StripeClient stripeClient) {
        this.courseRepository = courseRepository;
        this.accessService = accessService;
        this.subscriptionRepository = subscriptionRepository;
        this.stripeClient = stripeClient;
    }

    @Transactional
    public String createCheckout(Long courseId, Long userId, String email) {
        accessService.requireAdmin(courseId, userId);
        var course = courseRepository.findById(courseId).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Course not found"));
        subscriptionRepository.findByCourseId(courseId).orElseGet(() -> {
            Subscription sub = new Subscription();
            sub.setCourse(course);
            sub.setStatus(SubscriptionStatus.INCOMPLETE);
            return subscriptionRepository.save(sub);
        });
        return stripeClient.createSubscriptionCheckoutSession(courseId, email).url();
    }

    @Transactional
    public void updateSubscription(String stripeSubscriptionId, String stripeCustomerId, SubscriptionStatus status, Long currentPeriodEndEpoch) {
        Subscription sub = subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
                .or(() -> subscriptionRepository.findByStripeCustomerId(stripeCustomerId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Subscription not found"));
        sub.setStripeSubscriptionId(stripeSubscriptionId);
        sub.setStripeCustomerId(stripeCustomerId);
        sub.setStatus(status);
        if (currentPeriodEndEpoch != null) {
            sub.setCurrentPeriodEnd(java.time.Instant.ofEpochSecond(currentPeriodEndEpoch));
        }
        subscriptionRepository.save(sub);
    }
}
