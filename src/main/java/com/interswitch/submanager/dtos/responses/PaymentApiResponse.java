package com.interswitch.submanager.dtos.responses;

import lombok.*;

import java.io.Serializable;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PaymentApiResponse implements Serializable {
    private boolean successful;
    private String status;
    private String message;
    private int result;
    private SubscriptionPaymentResponse subscriptionPaymentResponse;
}
