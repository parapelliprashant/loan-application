package com.loanapplication.service.impl;

import com.loanapplication.dto.CustomerOnboardingRequest;
import com.loanapplication.dto.CustomerOnboardingStatusResponse;
import com.loanapplication.dto.CustomerResponse;
import com.loanapplication.entities.Customer;
import com.loanapplication.repo.CustomerRepository;
import com.loanapplication.service.CustomerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public CustomerResponse getCustomerByEmail(String email) {

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));

        return mapToResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(
            String email,
            CustomerOnboardingRequest request) {

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setAge(request.getAge());
        customer.setMobileNo(request.getMobileNo());
        customer.setAadhaarNo(request.getAadhaarNo());
        customer.setPan(request.getPan());
        customer.setEmploymentType(request.getEmploymentType());
        customer.setMonthlyIncome(request.getMonthlyIncome());
        customer.setMonthlyExpense(request.getMonthlyExpense());

        Customer savedCustomer = customerRepository.save(customer);

        return mapToResponse(savedCustomer);
    }


    @Override
    @Transactional(readOnly = true)
    public CustomerOnboardingStatusResponse getOnboardingStatus(String email) {

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found"));

        boolean profileComplete =
                customer.getFirstName() != null
                        && !customer.getFirstName().isBlank()
                        && customer.getLastName() != null
                        && !customer.getLastName().isBlank()
                        && customer.getAge() != null
                        && customer.getMobileNo() != null
                        && !customer.getMobileNo().isBlank()
                        && customer.getAadhaarNo() != null
                        && !customer.getAadhaarNo().isBlank()
                        && customer.getPan() != null
                        && !customer.getPan().isBlank()
                        && customer.getEmploymentType() != null
                        && !customer.getEmploymentType().isBlank()
                        && customer.getMonthlyIncome() != null
                        && customer.getMonthlyExpense() != null;

        CustomerOnboardingStatusResponse response =
                new CustomerOnboardingStatusResponse();

        response.setCustomerId(customer.getCustomerId());
        response.setProfileComplete(profileComplete);
        response.setEmailVerified(Boolean.TRUE.equals(customer.getEmailVerified()));

        response.setStatus(
                !profileComplete ? "PROFILE_INCOMPLETE"
                        : !Boolean.TRUE.equals(customer.getEmailVerified())
                        ? "EMAIL_VERIFICATION_PENDING"
                        : "COMPLETED"
        );

        return response;
    }

    private CustomerResponse mapToResponse(Customer customer) {

        CustomerResponse response = new CustomerResponse();

        response.setCustomerId(customer.getCustomerId());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setAge(customer.getAge());
        response.setEmail(customer.getEmail());
        response.setMobileNo(customer.getMobileNo());
        response.setAadhaarNo(customer.getAadhaarNo());
        response.setPan(customer.getPan());
        response.setEmploymentType(customer.getEmploymentType());
        response.setMonthlyIncome(customer.getMonthlyIncome());
        response.setMonthlyExpense(customer.getMonthlyExpense());
        response.setEmailVerified(customer.getEmailVerified());

        return response;
    }
}