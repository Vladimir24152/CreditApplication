package org.neoflex.calculator.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "loan.calculator")
public class LoanCalculatorProperties {

    private BigDecimal baseRate;

    private BigDecimal insuranceCostPercent;

    private BigDecimal insuranceRateDiscount = new BigDecimal("2.0");

    private BigDecimal salaryClientDiscount = new BigDecimal("1.0");
}
