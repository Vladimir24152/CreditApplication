package org.neoflex.calculator.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.calculator.constants.LoanCalculatorConstants;
import org.neoflex.calculator.dto.LoanStatementRequestDto;
import org.neoflex.calculator.dto.response.LoanOfferDto;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.neoflex.calculator.service.CreditCalculator.PERCENT_DIVISOR;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class LoanOfferService {

    private final CreditCalculator creditCalculator;

    public List<LoanOfferDto> calculateLoanOffers(LoanStatementRequestDto request){

        if (request == null) {
            throw new NullPointerException("Отсутствует тело запроса");
        }

        log.info("Получен запрос на расчет кредитных предложений: сумма={}, срок={} мес, имя={}, фамилия={}",
                request.getAmount(), request.getTerm(), request.getFirstName(), request.getLastName());

        List<LoanOfferDto> offers = new ArrayList<>();

        log.debug("Создание 4 кредитных предложений с различными комбинациями страховки и зарплатного клиента");

        offers.add(createOffer(request, true,true));
        offers.add(createOffer(request, false,true));
        offers.add(createOffer(request, true,false));
        offers.add(createOffer(request, false,false));

        log.info("Успешно сгенерировано {} кредитных предложений", offers.size());

        return offers.stream()
                .sorted(Comparator.comparing(LoanOfferDto::getRate).reversed())
                .peek(offer -> log.info("Предложение: страховка = {}, зарплатный клиент = {}, процентная ставка = {}",
                        offer.getIsInsuranceEnabled(),offer.getIsSalaryClient(),offer.getRate()))
                .collect(Collectors.toList());
    }

    private LoanOfferDto createOffer(LoanStatementRequestDto request, Boolean isInsuranceEnabled, Boolean isSalaryClient) {

        BigDecimal rate = calculateRate(isInsuranceEnabled, isSalaryClient);

        BigDecimal monthlyPayment = calculateMonthlyPayment(request.getAmount(), request.getTerm(), rate, isInsuranceEnabled);

        BigDecimal totalAmount = calculateTotalAmount(monthlyPayment,request.getTerm());

        return LoanOfferDto.builder()
                .statementId(UUID.randomUUID())
                .requestedAmount(request.getAmount().setScale(CreditCalculator.RESULT_SCALE, RoundingMode.HALF_UP))
                .totalAmount(totalAmount.setScale(CreditCalculator.RESULT_SCALE, RoundingMode.HALF_UP))
                .term(request.getTerm())
                .monthlyPayment(monthlyPayment.setScale(CreditCalculator.RESULT_SCALE, RoundingMode.HALF_UP))
                .rate(rate.setScale(CreditCalculator.RESULT_SCALE, RoundingMode.HALF_UP))
                .isInsuranceEnabled(isInsuranceEnabled)
                .isSalaryClient(isSalaryClient)
                .build();
    }

    private BigDecimal calculateRate(@NotNull(message = "Флаг страховки обязателен") Boolean isInsuranceEnabled,
                                     @NotNull(message = "Флаг зарплатного клиента обязателен") Boolean isSalaryClient) {
        BigDecimal rate = LoanCalculatorConstants.BASE_RATE;

        if (isInsuranceEnabled) {
            rate = rate.subtract(LoanCalculatorConstants.INSURANCE_RATE_DISCOUNT);
        }

        if (isSalaryClient) {
            rate = rate.subtract(LoanCalculatorConstants.SALARY_CLIENT_DISCOUNT);
        }

        rate = rate.setScale(2, RoundingMode.HALF_UP);

        log.debug("Предварительная процентная ставка с учетом скидок за страховку и флага зарплатного клиента = {}%", rate);

        return rate;
    }

    private BigDecimal calculateMonthlyPayment(BigDecimal amount, Integer term, BigDecimal rate, Boolean isInsuranceEnabled) {

        BigDecimal monthlyPayment = creditCalculator.calculateMonthlyPayment(amount,term,rate);

        if (isInsuranceEnabled){
            monthlyPayment = monthlyPayment.add(amount.multiply(LoanCalculatorConstants.INSURANCE_COST_PERCENT
                            .divide(PERCENT_DIVISOR,CreditCalculator.CALC_SCALE, RoundingMode.HALF_UP))
                    .divide(new BigDecimal(term),CreditCalculator.CALC_SCALE, RoundingMode.HALF_UP));
        }

        log.debug("Предварительный расчет ежемесячного аннуитетного платежа = {} руб.", monthlyPayment);

        return monthlyPayment;
    }

    private BigDecimal calculateTotalAmount(BigDecimal monthlyPayment, Integer term) {
        BigDecimal totalAmount = monthlyPayment.multiply(new BigDecimal(term)).setScale(CreditCalculator.CALC_SCALE, RoundingMode.HALF_UP);

        log.debug("Предварительная стоимость кредита с учетом страховки = {}", totalAmount);
        return totalAmount;
    }
}
