package org.neoflex.calculator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.calculator.config.LoanCalculatorProperties;
import org.neoflex.calculator.dto.CreditDto;
import org.neoflex.calculator.dto.PaymentScheduleElementDto;
import org.neoflex.calculator.dto.ScoringDataDto;
import org.neoflex.calculator.exception.ScoringFailed;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

import static org.neoflex.calculator.enums.EmploymentStatus.UNEMPLOYED;

/**
 * Сервис для расчета кредитных параметров и проведения скоринга клиентов.
 * <p>
 * Выполняет следующие функции:
 * <ul>
 *   <li>Валидацию заявки на кредит (возраст, стаж, доход)</li>
 *   <li>Расчет итоговой процентной ставки с учетом различных факторов</li>
 *   <li>Вычисление ежемесячного аннуитетного платежа</li>
 *   <li>Построение графика платежей</li>
 *   <li>Расчет полной стоимости кредита (ПСК)</li>
 * </ul>

 * @see ScoringDataDto
 * @see CreditDto
 * @see LoanCalculatorProperties
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CreditCalculationService {

    /** Количество месяцев в году */
    private static final BigDecimal MONTHS_IN_YEAR = new BigDecimal("12");

    /** Делитель для перевода процентов в доли (100) */
    private static final BigDecimal PERCENT_DIVISOR = new BigDecimal("100");

    /** Максимальное соотношение суммы кредита к ежемесячному доходу */
    private static final BigDecimal MAXIMUM_LOAN_AMOUNT_IN_SALARIES = new BigDecimal("24");

    /** Максимальный допустимый возраст заемщика */
    private static final int MAX_AGE_FOR_LOAD = 65;

    /** Минимальный допустимый возраст заемщика */
    private static final int MIN_AGE_FOR_LOAD = 20;

    /** Минимальный общий трудовой стаж (в месяцах) */
    private static final int MIN_EXPERIENCE_TOTAL_FOR_LOAD = 18;

    /** Минимальный текущий стаж на последнем месте работы (в месяцах) */
    private static final int MIN_EXPERIENCE_CURRENT_FOR_LOAD = 3;

    /** Точность промежуточных вычислений (количество знаков после запятой) */
    private static final int CALC_SCALE = 5;

    /** Точность финальных результатов (количество знаков после запятой) */
    private static final int RESULT_SCALE = 2;

    /** Класс содержащий свойства для подсчета кредитных условий */
    private final LoanCalculatorProperties calculatorProperties;

    /**
     * Основной метод расчета кредита на основе данных скоринга.
     * <p>
     * Последовательность расчета:
     * <ol>
     *   <li>Проверка заявки на соответствие базовым требованиям</li>
     *   <li>Расчет итоговой процентной ставки с учетом всех факторов</li>
     *   <li>Вычисление ежемесячного платежа</li>
     *   <li>Расчет полной стоимости кредита (ПСК)</li>
     *   <li>Построение графика платежей</li>
     *   <li>Формирование итогового DTO с результатами</li>
     * </ol>
     *
     * @param request объект с данными для скоринга
     * @return CreditDto с полной информацией о рассчитанном кредите
     * @throws ScoringFailed если заявка не проходит скоринг
     */
    public CreditDto calculateCredit(ScoringDataDto request) {

        log.info("Получен запрос на расчет кредитных условий: сумма={}, срок={} мес, имя={}, фамилия={}",
                request.getAmount(), request.getTerm(), request.getFirstName(), request.getLastName());

        checkingTheLoanApplication(request);

        BigDecimal finalRate = calculateTotalRate(request);

        BigDecimal monthlyPayment = calculateMonthlyPayment(
                request.getAmount(),
                request.getTerm(),
                finalRate,
                request.getIsInsuranceEnabled()
        );

        BigDecimal psk = calculatePsk(monthlyPayment, request.getTerm());

        List<PaymentScheduleElementDto> paymentSchedule = calculatePaymentSchedule(
                request.getAmount(),
                finalRate,
                request.getTerm(),
                monthlyPayment
        );

        CreditDto creditDto = CreditDto.builder()
                .amount(request.getAmount().setScale(RESULT_SCALE, RoundingMode.HALF_UP))
                .term(request.getTerm())
                .monthlyPayment(monthlyPayment.setScale(RESULT_SCALE, RoundingMode.HALF_UP))
                .rate(finalRate.setScale(RESULT_SCALE, RoundingMode.HALF_UP))
                .psk(psk.setScale(RESULT_SCALE, RoundingMode.HALF_UP))
                .isInsuranceEnabled(request.getIsInsuranceEnabled())
                .isSalaryClient(request.getIsSalaryClient())
                .paymentSchedule(paymentSchedule)
                .build();

        log.info("Составлено кредитное предложение: сумма={}, ПСК={}, срок={} мес, процентная ставка={}",
                creditDto.getAmount(),creditDto.getPsk(), creditDto.getTerm(), creditDto.getRate());

        return creditDto;
    }

    /**
     * Проверка кредитной заявки на соответствие базовым требованиям.
     * <p>
     * Выполняет следующие проверки:
     * <ul>
     *   <li>Статус занятости (не безработный)</li>
     *   <li>Соотношение суммы кредита к доходу</li>
     *   <li>Возраст заемщика (20-65 лет)</li>
     *   <li>Общий трудовой стаж (не менее 18 месяцев)</li>
     *   <li>Текущий стаж (не менее 3 месяцев)</li>
     * </ul>
     *
     * @param request объект с данными для скоринга
     * @throws ScoringFailed если заявка не проходит хотя бы одну проверку
     */
    private void checkingTheLoanApplication(ScoringDataDto request) {
        if (request.getEmployment().getEmploymentStatus().equals(UNEMPLOYED)){
            throw new ScoringFailed("Отказ в предоставлении займа не трудоустроенным");
        }

        if (request.getEmployment().getSalary().multiply(MAXIMUM_LOAN_AMOUNT_IN_SALARIES).compareTo(request.getAmount()) < 0){
            throw new ScoringFailed("Отказ в предоставлении займа превышающего среднемесячный доход более чем в "
                    + MAXIMUM_LOAN_AMOUNT_IN_SALARIES + "раза(раз)");
        }

        int age = Period.between(request.getBirthDate(), LocalDate.now()).getYears();

        if (age < MIN_AGE_FOR_LOAD){
            throw new ScoringFailed(String.format("Отказ в предоставлении займа клиентам младше %d лет",MIN_AGE_FOR_LOAD));
        }

        if (age > MAX_AGE_FOR_LOAD){
            throw new ScoringFailed(String.format("Отказ в предоставлении займа клиентам старше %d лет",MAX_AGE_FOR_LOAD));
        }

        if (request.getEmployment().getWorkExperienceTotal() < MIN_EXPERIENCE_TOTAL_FOR_LOAD){
            throw new ScoringFailed(String.format("Отказ в предоставлении займа клиентам с общим стажем работы менее %d месяцев",MIN_EXPERIENCE_TOTAL_FOR_LOAD));
        }

        if (request.getEmployment().getWorkExperienceCurrent() < MIN_EXPERIENCE_CURRENT_FOR_LOAD){
            throw new ScoringFailed(String.format("Отказ в предоставлении займа клиентам с текущем стажем работы менее %d месяцев",MIN_EXPERIENCE_CURRENT_FOR_LOAD));
        }
    }

    /**
     * Расчет итоговой процентной ставки с учетом всех факторов скоринга.
     * <p>
     * Факторы, влияющие на ставку:
     * <ul>
     *   <li>Статус занятости (самозанятый/владелец бизнеса - повышение)</li>
     *   <li>Должность (руководители - снижение)</li>
     *   <li>Семейное положение (женат - снижение, разведен - повышение)</li>
     *   <li>Возраст и пол (льготные возрастные категории)</li>
     *   <li>Наличие страховки (снижение)</li>
     * </ul>
     *
     * @param request объект с данными для скоринга
     * @return итоговая процентная ставка (в процентах годовых)
     */
    private BigDecimal calculateTotalRate(ScoringDataDto request) {

        BigDecimal finalRate = calculatorProperties.getBaseRate();

        finalRate = switch (request.getEmployment().getEmploymentStatus()) {
            case SELF_EMPLOYED -> finalRate.add(calculatorProperties.getSelfEmploeRateAdd());
            case BUSINESS_OWNER -> finalRate.add(calculatorProperties.getBusinesOwnerRateAdd());
            default -> finalRate;
        };

        finalRate = switch (request.getEmployment().getPosition()) {
            case MID_MANAGER -> finalRate.subtract(calculatorProperties.getMidManagerRateDiscount());
            case TOP_MANAGER -> finalRate.subtract(calculatorProperties.getTopManagerRateDiscount());
            default -> finalRate;
        };

        finalRate = switch (request.getMaritalStatus()) {
            case MARRIED -> finalRate.subtract(calculatorProperties.getMarriedRateDiscount());
            case DIVORCED -> finalRate.add(calculatorProperties.getDivorcedRateAdd());
            default -> finalRate;
        };

        int age = Period.between(request.getBirthDate(), LocalDate.now()).getYears();

        switch (request.getGender()){
            case MALE:
                if (age >= 30 && age <= 55) {
                    finalRate = finalRate.subtract(calculatorProperties.getMaleRateDiscount());
                }
                break;
            case FEMALE:
                if (age >= 32 && age <= 60) {
                    finalRate = finalRate.subtract(calculatorProperties.getFemaleRateDiscount());
                }
                break;
            case NOT_BINARY:
                finalRate = finalRate.add(calculatorProperties.getNotBinaryRateAdd());
                break;
        }

        if (request.getIsInsuranceEnabled()){
            finalRate = finalRate.subtract(calculatorProperties.getInsuranceRateDiscount());
        }

        log.debug("Процентная ставка по кредиту расчитана в сумме={}",finalRate);
        return finalRate;
    }

    /**
     * Расчет ежемесячного аннуитетного платежа.
     * <p>
     * Формула расчета:
     * Платеж = Сумма × (мес_ставка × (1 + мес_ставка)^срок) / ((1 + мес_ставка)^срок - 1)
     * <p>
     * При наличии страховки к платежу добавляется стоимость страховки,
     * распределенная равномерно на весь срок кредита.
     *
     * @param amount сумма кредита
     * @param term срок кредита в месяцах
     * @param finalRate годовая процентная ставка
     * @param getIsInsuranceEnabled флаг наличия страховки
     * @return сумма ежемесячного платежа
     */
    private BigDecimal calculateMonthlyPayment(BigDecimal amount, Integer term, BigDecimal finalRate, Boolean getIsInsuranceEnabled) {
        BigDecimal monthlyRate = finalRate
                .divide(PERCENT_DIVISOR, CALC_SCALE, RoundingMode.HALF_UP)
                .divide(MONTHS_IN_YEAR, CALC_SCALE, RoundingMode.HALF_UP);

        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);

        BigDecimal annuityRatio = monthlyRate.multiply(onePlusRate.pow(term))
                .divide(onePlusRate.pow(term).subtract(BigDecimal.ONE), CALC_SCALE, RoundingMode.HALF_UP);

        BigDecimal monthlyPayment =amount.multiply(annuityRatio).setScale(RESULT_SCALE, RoundingMode.HALF_UP);

        if (getIsInsuranceEnabled){
            monthlyPayment = monthlyPayment.add(amount.multiply(calculatorProperties.getInsuranceCostPercent().divide(PERCENT_DIVISOR,CALC_SCALE, RoundingMode.HALF_UP))
                    .divide(new BigDecimal(term),CALC_SCALE, RoundingMode.HALF_UP));
        }

        log.debug("Аннуитетный платежа по кредиту={}",monthlyPayment);
        return monthlyPayment;
    }

    /**
     * Расчет полной стоимости кредита (ПСК) в денежном выражении.
     * <p>
     * ПСК представляет собой общую сумму всех платежей по кредиту за весь срок.
     *
     * @param monthlyPayment ежемесячный платеж
     * @param term срок кредита в месяцах
     * @return полная стоимость кредита в рублях
     */
    private BigDecimal calculatePsk(BigDecimal monthlyPayment, Integer term) {
        BigDecimal psk = monthlyPayment.multiply(new BigDecimal(term));
        log.debug("Полная стоимость кредита={}",psk);
        return psk;
    }

    /**
     * Построение графика аннуитетных платежей.
     * <p>
     * Для каждого месяца рассчитываются:
     * <ul>
     *   <li>Дата платежа</li>
     *   <li>Сумма платежа (общая)</li>
     *   <li>Сумма погашения основного долга</li>
     *   <li>Сумма уплаченных процентов</li>
     *   <li>Остаток задолженности после платежа</li>
     * </ul>
     * <p>
     * Особенности расчета:
     * <ul>
     *   <li>Проценты начисляются на остаток долга</li>
     *   <li>В последнем месяце сумма корректируется для полного погашения</li>
     *   <li>Даты платежей устанавливаются на одно число каждого месяца</li>
     * </ul>
     *
     * @param amount сумма кредита
     * @param rate годовая процентная ставка
     * @param term срок кредита в месяцах
     * @param monthlyPayment ежемесячный платеж
     * @return список элементов графика платежей
     */
    private List<PaymentScheduleElementDto> calculatePaymentSchedule(
            BigDecimal amount,
            BigDecimal rate,
            Integer term,
            BigDecimal monthlyPayment) {

        List<PaymentScheduleElementDto> schedule = new ArrayList<>();

        BigDecimal monthlyRate = rate
                .divide(PERCENT_DIVISOR, CALC_SCALE, RoundingMode.HALF_UP)
                .divide(MONTHS_IN_YEAR, CALC_SCALE, RoundingMode.HALF_UP);

        BigDecimal remainingDebt = amount;

        LocalDate paymentDate = LocalDate.now().plusMonths(1);

        for (int i = 1; i <= term; i++) {
            BigDecimal interestPayment = remainingDebt
                    .multiply(monthlyRate)
                    .setScale(RESULT_SCALE, RoundingMode.HALF_UP);

            BigDecimal principalPayment;
            BigDecimal totalPayment;

            if (i == term) {
                principalPayment = remainingDebt;
                totalPayment = principalPayment.add(interestPayment);
            } else {
                principalPayment = monthlyPayment.subtract(interestPayment);
                totalPayment = monthlyPayment;
            }

            remainingDebt = remainingDebt.subtract(principalPayment);

            PaymentScheduleElementDto element = PaymentScheduleElementDto.builder()
                    .number(i)
                    .date(paymentDate)
                    .totalPayment(totalPayment.setScale(RESULT_SCALE, RoundingMode.HALF_UP))
                    .principalPayment(principalPayment.setScale(RESULT_SCALE, RoundingMode.HALF_UP))
                    .interestPayment(interestPayment.setScale(RESULT_SCALE, RoundingMode.HALF_UP))
                    .remainingDebt(remainingDebt.setScale(RESULT_SCALE, RoundingMode.HALF_UP))
                    .build();

            schedule.add(element);

            paymentDate = paymentDate.plusMonths(1);
        }

        return schedule;
    }
}
