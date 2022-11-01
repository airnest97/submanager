package com.interswitch.submanager.dtos.requests;

import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GotvWalletPaymentRequest {
    private String subscriptionId;
    private BigDecimal priceOfSubscription;
    private String walletId;
    private String fullName;
    private String phoneNumber;
    private String IUC_Number;
}
