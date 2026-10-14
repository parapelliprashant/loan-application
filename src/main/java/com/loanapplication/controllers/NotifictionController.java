package com.loanapplication.controllers;

import com.loanapplication.entities.Notification;
import com.loanapplication.helper.ApiResponse;
import com.loanapplication.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/api/v1/loan")
@RequiredArgsConstructor
@Controller
public class NotifictionController {


    private final NotificationService notificationService;

    @GetMapping("notifications/{customerId}")
    public ResponseEntity<ApiResponse< List<Notification>>> fetchNotificationById(@PathVariable Integer customerId)
    {
        List<Notification> notificationList =  notificationService.getNotifictaionByCustomerId(customerId);
        ApiResponse<List<Notification>> response =                      new ApiResponse<>(true,"Notification Fetched SucessFully",notificationList,null);
       return ResponseEntity.ok(response);
    }



}
