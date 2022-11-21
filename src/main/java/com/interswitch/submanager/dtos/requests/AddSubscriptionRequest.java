package com.interswitch.submanager.dtos.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.interswitch.submanager.models.enums.Category;
import com.interswitch.submanager.models.enums.Cycle;
import com.interswitch.submanager.models.enums.RecurringPayment;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddSubscriptionRequest {

    private String nameOfSubscription;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal priceOfSubscription;

    private Category category;

    private String description;

    private RecurringPayment recurringPayment;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate nextPayment;

    private Cycle paymentCycle;
}
