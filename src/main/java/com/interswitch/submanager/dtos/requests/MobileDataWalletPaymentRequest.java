package com.interswitch.submanager.dtos.requests;


import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MobileDataWalletPaymentRequest {
    private String subscriptionId;
    private BigDecimal priceOfSubscription;
    private String walletId;
    private String fullName;
    private String phoneNumber;
    private String mobileNetwork;
}