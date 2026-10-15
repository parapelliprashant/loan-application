package com.loanapplication.service;

import com.loanapplication.dto.CustomerOnboardingRequest;
import com.loanapplication.dto.CustomerOnboardingStatusResponse;
import com.loanapplication.dto.CustomerResponse;

public interface CustomerService {

    CustomerResponse getCustomerByEmail(String email);

    CustomerResponse updateCustomer(
            String email,
            CustomerOnboardingRequest request
    );

    CustomerOnboardingStatusResponse getOnboardingStatus(String email);
}