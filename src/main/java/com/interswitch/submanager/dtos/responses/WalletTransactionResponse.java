package com.interswitch.submanager.dtos.responses;

import com.interswitch.submanager.models.data.Data;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WalletTransactionResponse {
    private String status;
    private String message;
    private Data data;
}
