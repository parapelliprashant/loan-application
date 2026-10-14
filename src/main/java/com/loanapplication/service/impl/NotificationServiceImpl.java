package com.loanapplication.service.impl;

import com.loanapplication.entities.Notification;
import com.loanapplication.repo.NotificationRepo;
import com.loanapplication.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepo notificationRepo;

    @Override
    public List<Notification> getNotifictaionByCustomerId(Integer customerId) {

      List<Notification> notificationList =  notificationRepo.findByCustomer_CustomerId(customerId);

        return notificationList;

    }
}
