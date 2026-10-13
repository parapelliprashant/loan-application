package com.loanapplication.repo;

import com.loanapplication.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepo
        extends JpaRepository<Customer, Integer> {
}