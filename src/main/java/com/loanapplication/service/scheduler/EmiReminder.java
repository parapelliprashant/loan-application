package com.loanapplication.service.scheduler;


import com.loanapplication.dto.NotificationRequestDto;
import com.loanapplication.entities.EmiSchedule;
import com.loanapplication.entities.LoanAccount;
import com.loanapplication.entities.Notification;
import com.loanapplication.repo.EmiSchedulerRepo;
import com.loanapplication.repo.NotificationRepo;
import com.loanapplication.service.email.EmailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmiReminder {

    private final EmiSchedulerRepo emiSchedulerRepo;
    private final JavaMailSender javaMailSender;
    private final ModelMapper modelMapper;
    private final NotificationRepo notificationRepo;
    private final EmailSenderService emailSenderService;



    @Value("${spring.mail.username}")
    private String  senderEmail;

    @Scheduled(cron = "0 0 8,18 * *  ?")
    public void executeEmiRemidersUsingCron()
    {
        log.info("Emi Scheduler Runned SucesFully");
        processDailyReminders();

    }
    public void processDailyReminders() {
        LocalDate todayDate = LocalDate.now();
        LocalDate emiDate = todayDate.plusDays(3);
        List<EmiSchedule> allUnpaidEmi = emiSchedulerRepo.sendNotificationsRegardingpaymentBeforeDueDateAndUnpaidEmis(emiDate);
        if (allUnpaidEmi.isEmpty()) {
            log.info("No Emi pending for Todays Date");
        }
        Map<LoanAccount, List<EmiSchedule>> distinctEmiPerLoanAccount = allUnpaidEmi.stream().collect(Collectors.groupingBy(EmiSchedule::getLoanAccount));
        for (Map.Entry<LoanAccount, List<EmiSchedule>> emiScheduleList : distinctEmiPerLoanAccount.entrySet()) {
            LoanAccount loanAccount = emiScheduleList.getKey();
            List<EmiSchedule> emiSchedules = emiScheduleList.getValue();

            emailSenderService.sendMail(senderEmail,loanAccount.getCustomer().getEmail(),"payEmail","Pay your remaning emi ");
            NotificationRequestDto notificationRequestDto = new NotificationRequestDto();
            notificationRequestDto.setCustomerId(loanAccount.getCustomer().getCustomerId());
            notificationRequestDto.setMessage("Pay your loan ");
            notificationRequestDto.setRead(false);
            notificationRequestDto.setCreatedAt(LocalDateTime.now());
            Notification notification = modelMapper.map(notificationRequestDto, Notification.class);
            notificationRepo.save(notification);
            return;
        }
    }
}



