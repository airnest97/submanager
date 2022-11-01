package com.interswitch.submanager.service.notification;

import com.interswitch.submanager.dtos.requests.EmailRequest;
import com.interswitch.submanager.dtos.requests.SmsRequest;
import com.interswitch.submanager.events.SendMessageEvent;
import com.interswitch.submanager.models.data.Subscription;
import com.interswitch.submanager.service.subscription.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
public class TaskScheduling {
    private final SubscriptionService subscriptionService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Async
    @Scheduled(cron = "0 0 */23 * * *")
    public void getSubToExpireInThreeDays() {

        List<Subscription> subscriptions = subscriptionService.findByDate(LocalDate.now());

        SmsRequest request = new SmsRequest();
        EmailRequest emailRequest = new EmailRequest();

        for (Subscription subscription : subscriptions) {
            request.setMessage(String.format("Reminder that your " + subscription.getNameOfSubscription()
                    .toUpperCase() + " subscription expires on " + subscription.getNextPayment()
                    + "thank you."));
            request.setReceiverPhoneNumber(subscription.getUser().getPhoneNumber());
            SendMessageEvent sendMessageEvent = new SendMessageEvent(request, "sms");
            applicationEventPublisher.publishEvent(sendMessageEvent);

            emailRequest.setBody(String.format("Reminder that your " + subscription.getNameOfSubscription()
                    .toUpperCase() + " subscription expires on " + subscription.getNextPayment()
                    + "thank you."));
            emailRequest.setReceiver(subscription.getUser().getEmail());
            SendMessageEvent sendNotificationEmail = new SendMessageEvent(emailRequest, "email");
            applicationEventPublisher.publishEvent(sendNotificationEmail);
        }
        log.info("scheduler executed at -> {} ", LocalDateTime.now());
    }
}
