package com.interswitch.submanager.service.notification.notify;

import com.interswitch.submanager.dtos.requests.MessageRequest;
import com.interswitch.submanager.dtos.responses.MailResponse;
import com.mailjet.client.errors.MailjetException;
import com.mashape.unirest.http.exceptions.UnirestException;


public interface EmailService {
    void sendMailjetEmail(MessageRequest emailRequest) throws MailjetException;

    MailResponse sendChampEmail(MessageRequest emailRequest) throws UnirestException;
}
