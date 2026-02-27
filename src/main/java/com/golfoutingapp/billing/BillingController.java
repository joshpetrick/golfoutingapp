package com.golfoutingapp.billing;

import com.golfoutingapp.config.CurrentUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/courses/{courseId}/billing")
public class BillingController {
    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("/checkout")
    public Map<String, String> checkout(@PathVariable Long courseId, Authentication auth) {
        CurrentUser user = (CurrentUser) auth.getPrincipal();
        return Map.of("url", billingService.createCheckout(courseId, user.id(), user.email()));
    }
}
