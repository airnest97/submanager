package com.interswitch.submanager.service.notification.notify;

import com.interswitch.submanager.dtos.requests.MessageRequest;
import com.interswitch.submanager.dtos.responses.MailResponse;
import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.errors.MailjetException;
import com.mailjet.client.transactional.SendContact;
import com.mailjet.client.transactional.SendEmailsRequest;
import com.mailjet.client.transactional.TrackOpens;
import com.mailjet.client.transactional.TransactionalEmail;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final String PASS = System.getenv("SEND_CHAMP_PASS");

    @Override
    public void sendMailjetEmail(MessageRequest emailRequest) throws MailjetException {
        ClientOptions options = ClientOptions
                .builder()
                .apiKey(System.getenv("MAILJET_API_KEY"))
                .apiSecretKey(System.getenv("MAILJET_SECRET_KEY"))
                .build();
        MailjetClient client = new MailjetClient(options);

        TransactionalEmail sendMessage = TransactionalEmail
                .builder()
                .textPart(emailRequest.getBody())
                .to(new SendContact(emailRequest.getReceiver()))
                .from(new SendContact(emailRequest.getSender()))
                .htmlPart("")
                .subject(emailRequest.getSubject())
                .trackOpens(TrackOpens.ENABLED)
                .header("","")
                .customID("")
                .build();

        SendEmailsRequest request = SendEmailsRequest
                .builder()
                .message(sendMessage)
                .build();
        request.sendWith(client);
    }

    @Override
    public MailResponse sendChampEmail(MessageRequest emailRequest) throws UnirestException {
        HttpResponse<String> response = Unirest.post("https://api.sendchamp.com/api/v1/email/send")
                .header("accept", "application/json")
                .header("content-type", "application/json")
                .header("Authorization","Bearer " + PASS)
                .body(("{\"to\":[{\"email\":\"" + emailRequest.getReceiver() + "\"}]," +
                        "\"from\":{\"email\":\"" + emailRequest.getSender() + "\"," +
                        "\"name\":\"SAlert\"}," +
                        "\"message_body\":{\"type\":\"text/html\"," +
                        "\"value\":\"" + emailRequest.getBody() + "\"}," +
                        "\"subject\":\"" + emailRequest.getSubject() + "\"}"))
                .asString();
        return response.getStatus() == 200 ? new MailResponse(true) : new MailResponse(false);
    }
}