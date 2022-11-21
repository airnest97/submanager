package com.interswitch.submanager.models.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.interswitch.submanager.models.enums.Category;
import com.interswitch.submanager.models.enums.Cycle;
import com.interswitch.submanager.models.enums.RecurringPayment;
import lombok.*;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
@Table
@ToString
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nameOfSubscription;

    @Enumerated(value = EnumType.STRING)
    private Category category;

    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal priceOfSubscription;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateAdded;

    @Enumerated(value = EnumType.STRING)
    private RecurringPayment recurringPayment;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate nextPayment;

    @Enumerated(value = EnumType.STRING)
    private Cycle paymentCycle;
    @ManyToOne
    private User user;
}
