package org.neoflex.deal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.neoflex.deal.dto.LoanOfferDto;
import org.neoflex.deal.model.enums.ApplicationStatus;
import org.neoflex.deal.model.jsonb.StatusHistory;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "statement")
public class Statement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Comment("Идентификатор заявления")
    @Column(name = "statement_id")
    private UUID statement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false, unique = true)
    @Comment("Идентификатор клиента")
    private Client client;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credit_id", nullable = false, unique = true)
    @Comment("Идентификатор кредита")
    private Credit credit;

    @Enumerated(EnumType.STRING)
    @Comment("Статус заявки")
    @Column(name = "status", nullable = false)
    private ApplicationStatus status;

    @CreationTimestamp
    @Comment("Дата создания заявки")
    @Column(name = "creation_date", updatable = false, nullable = false)
    private LocalDateTime creationDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("Предложение по кредиту")
    @Column(name = "applied_offer",columnDefinition = "jsonb", nullable = false)
    private LoanOfferDto appliedOffer;//Не понимаю что это уточнить!!! Нет на схеме

    @CreationTimestamp
    @Comment("Дата исполнения заявки")
    @Column(name = "sign_date")
    private LocalDateTime signDate;

    @Comment("Не понимаю что это уточнить")//Не понимаю что это уточнить!!!
    @Column(name = "ses_сode")
    private String sesCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("Статус истории")
    @Column(name = "status_history",columnDefinition = "jsonb")//Не понимаю что это уточнить!!! может быть null или нет?
    private StatusHistory statusHistory;
}
