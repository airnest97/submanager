package com.interswitch.submanager.dtos.requests;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class CreateWalletRequest {
    private BigDecimal balance;
}
