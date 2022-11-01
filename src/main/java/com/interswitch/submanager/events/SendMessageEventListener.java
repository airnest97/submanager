package com.interswitch.submanager.events;

import com.interswitch.submanager.dtos.requests.SmsRequest;
import com.interswitch.submanager.dtos.requests.MessageRequest;
import com.interswitch.submanager.dtos.responses.SMSResponse;
import com.interswitch.submanager.service.notification.notify.EmailService;
import com.interswitch.submanager.service.notification.notify.SmsService;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SendMessageEventListener {
    private final SmsService smsService;
    private final EmailService emailService;


    @EventListener
    public void handleSendEmailEvent(SendMessageEvent event) throws UnirestException {
        if (event.getType().equals("sms")) {
            SmsRequest smsRequest = (SmsRequest) event.getSource();
            SMSResponse response = smsService.sendSms(smsRequest);
            log.info("reminder was sent to -> {} ", smsRequest.getReceiverPhoneNumber());
            log.info("sms was sent -> {}", response.isSuccessful());
        } else {
            MessageRequest emailRequest = (MessageRequest) event.getSource();
            emailService.sendChampEmail(emailRequest);
            log.info("email was sent to -> {} ", emailRequest.getReceiver());
        }
    }
}
