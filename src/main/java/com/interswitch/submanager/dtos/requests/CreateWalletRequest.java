package com.interswitch.submanager.dtos.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class CreateWalletRequest {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal balance;
}
