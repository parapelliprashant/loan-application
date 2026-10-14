package com.loanapplication.service;

import com.loanapplication.entities.Notification;

import java.util.List;

public interface NotificationService {

    public List<Notification> getNotifictaionByCustomerId(Integer id);

}
