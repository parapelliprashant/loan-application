package com.loanapplication.controllers;

import com.loanapplication.dto.CustomerOnboardingRequest;
import com.loanapplication.dto.CustomerOnboardingStatusResponse;
import com.loanapplication.dto.CustomerResponse;
import com.loanapplication.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/me")
    public CustomerResponse getMyProfile(
            Authentication authentication) {

        String email = authentication.getName();

        return customerService.getCustomerByEmail(email);
    }

    @PutMapping("/me")
    public CustomerResponse updateMyProfile(
            Authentication authentication,
            @RequestBody CustomerOnboardingRequest request) {

        String email = authentication.getName();

        return customerService.updateCustomer(
                email,
                request
        );
    }


    @GetMapping("/me/onboarding-status")
    public ResponseEntity<CustomerOnboardingStatusResponse> getMyOnboardingStatus(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                customerService.getOnboardingStatus(email)
        );
    }

}