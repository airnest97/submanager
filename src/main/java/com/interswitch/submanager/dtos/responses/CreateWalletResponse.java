package com.interswitch.submanager.dtos.responses;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class CreateWalletResponse {
    private boolean message;
    private long walletId;
}
