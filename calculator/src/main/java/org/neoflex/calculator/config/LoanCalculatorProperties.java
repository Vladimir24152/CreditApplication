package org.neoflex.calculator.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Getter
@Setter
@ConfigurationProperties(prefix = "loan.calculator")
public class LoanCalculatorProperties {

    private BigDecimal baseRate;

    private BigDecimal insuranceCostPercent;

    private BigDecimal insuranceRateDiscount;

    private BigDecimal salaryClientDiscount;

    private BigDecimal selfEmployRateAdd;

    private BigDecimal businessOwnerRateAdd;

    private BigDecimal midManagerRateDiscount;

    private BigDecimal topManagerRateDiscount;

    private BigDecimal marriedRateDiscount;

    private BigDecimal divorcedRateAdd;

    private BigDecimal maleRateDiscount;

    private BigDecimal femaleRateDiscount;

    private BigDecimal notBinaryRateAdd;
}
