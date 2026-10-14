package com.loanapplication.repo;

import com.loanapplication.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface NotificationRepo extends JpaRepository<Notification,Integer> {

  List<Notification> findByCustomer_CustomerId(Integer customerId);

}
