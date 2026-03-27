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
import org.neoflex.deal.model.enums.Gender;
import org.neoflex.deal.model.enums.MaritalStatus;
import org.neoflex.deal.model.jsonb.Employment;
import org.neoflex.deal.model.jsonb.Passport;

import java.time.LocalDate;
import java.util.UUID;


@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "client")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Comment("Идентификатор клиента")
    @Column(name = "client_id")
    private UUID clientId;

    @Comment("Фамилия клиента")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Comment("Имя клиента")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Comment("Отчество клиента")
    @Column(name = "middle_name")
    private String middleName;

    @Comment("Дата рождения клиента")
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Comment("Email клиента")
    @Column(name = "email", nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Comment("Пол клиента")
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Comment("Семейное положение клиента")
    @Column(name = "marital_status", nullable = false)
    private MaritalStatus maritalStatus;

    @Comment("Количество людей находящихся на иждивении клиента")
    @Column(name = "dependent_amount", nullable = false)
    private Integer dependentAmount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("Паспортные данные клиента")
    @Column(name = "passport",columnDefinition = "jsonb", nullable = false)
    private Passport passport;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("Данные о трудоустройстве клиента")
    @Column(name = "employment",columnDefinition = "jsonb", nullable = false)
    private Employment employment;

    @Comment("Количество людей находящихся на иждивении клиента")
    @Column(name = "account_number", nullable = false)
    private String accountNumber;
}
