package org.neoflex.deal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.neoflex.deal.dto.PaymentScheduleElementDto;
import org.neoflex.deal.model.enums.CreditStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "credit")
public class Credit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Comment("Идентификатор кредита")
    @Column(name = "credit_id")
    private UUID creditId;

    @Comment("Сумма кредита")
    @Column(name = "amount",nullable = false)
    private BigDecimal amount;

    @Comment("Срок кредита в месяцах")
    @Column(name = "term",nullable = false)
    private Integer term;

    @Comment("Месячный платеж")
    @Column(name = "monthly_payment",nullable = false)
    private BigDecimal monthlyPayment;

    @Comment("Процентная ставка")
    @Column(name = "rate",nullable = false)
    private BigDecimal rate;

    @Comment("Полная стоимость кредита")
    @Column(name = "psk",nullable = false)
    private BigDecimal psk;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("График платежей")
    @Column(name = "payment_schedule",columnDefinition = "jsonb", nullable = false)
    private List<PaymentScheduleElementDto> paymentSchedule;

    @Comment("Флаг страховки кредита")
    @Column(name = "insurance_enabled",nullable = false)
    private Boolean isInsuranceEnabled;

    @Comment("Флаг зарплатного клиента")
    @Column(name = "salary_client",nullable = false)
    private Boolean isSalaryClient;

    @Enumerated(EnumType.STRING)
    @Comment("Статус кредита")
    @Column(name = "credit_status", nullable = false)
    private CreditStatus creditStatus;
}
