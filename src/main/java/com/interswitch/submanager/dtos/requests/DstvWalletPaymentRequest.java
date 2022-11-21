package com.interswitch.submanager.dtos.requests;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DstvWalletPaymentRequest {
    private String subscriptionId;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal priceOfSubscription;
    private String walletId;
    private String fullName;
    private String phoneNumber;
    private String smartCardNumber;
}
