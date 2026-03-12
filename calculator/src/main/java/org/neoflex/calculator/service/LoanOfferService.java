package org.neoflex.calculator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.calculator.config.LoanCalculatorProperties;
import org.neoflex.calculator.dto.LoanOfferDto;
import org.neoflex.calculator.dto.LoanStatementRequestDto;
import org.neoflex.calculator.exception.NotValidBirthDateException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Сервис для формирования кредитных предложений на основе базовых данных заявки.
 * <p>
 * Основные функции:
 * <ul>
 *   <li>Валидация даты рождения клиента</li>
 *   <li>Создание 4 вариантов кредитных предложений (комбинации страховки и зарплатного клиента)</li>
 *   <li>Расчет итоговой суммы кредита с учетом страховки</li>
 *   <li>Расчет процентной ставки с учетом скидок</li>
 *   <li>Расчет ежемесячного аннуитетного платежа</li>
 *   <li>Сортировка предложений от худшего к лучшему</li>
 * </ul>
 *
 * @see LoanOfferDto
 * @see LoanStatementRequestDto
 * @see LoanCalculatorProperties
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoanOfferService {

    /** Количество месяцев в году */
    private static final BigDecimal MONTHS_IN_YEAR = new BigDecimal("12");

    /** Делитель для перевода процентов в доли (100) */
    private static final BigDecimal PERCENT_DIVISOR = new BigDecimal("100");

    /** Точность промежуточных вычислений (количество знаков после запятой) */
    private static final int CALC_SCALE = 5;

    /** Точность финальных результатов (количество знаков после запятой) */
    private static final int RESULT_SCALE = 2;

    /** Класс содержащий свойства для подсчета кредитных условий */
    private final LoanCalculatorProperties calculatorProperties;

    /**
     * Расчет всех возможных кредитных предложений на основе заявки клиента.
     * <p>
     * Метод создает 4 предложения для всех комбинаций параметров:
     * <ol>
     *   <li>Со страховкой и зарплатным клиентом</li>
     *   <li>Без страховки, но с зарплатным клиентом</li>
     *   <li>Со страховкой, но без зарплатного клиента</li>
     *   <li>Без страховки и без зарплатного клиента</li>
     * </ol>
     * <p>
     * Предложения сортируются по убыванию процентной ставки (от худшего к лучшему).
     *
     * @param request DTO с базовыми данными заявки (сумма, срок, персональные данные)
     * @return список из 4 кредитных предложений, отсортированных от худшего к лучшему
     * @throws NotValidBirthDateException если возраст клиента менее 18 лет
     */
    public List<LoanOfferDto> calculateLoanOffers(LoanStatementRequestDto request){

        log.info("Получен запрос на расчет кредитных предложений: сумма={}, срок={} мес, имя={}, фамилия={}",
                request.getAmount(), request.getTerm(), request.getFirstName(), request.getLastName());

        isValidBirthDate(request);

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

    /**
     * Создание одного кредитного предложения с заданными параметрами.
     *
     * @param request базовые данные заявки
     * @param isInsuranceEnabled флаг наличия страховки
     * @param isSalaryClient флаг зарплатного клиента
     * @return заполненный DTO кредитного предложения
     */
    private LoanOfferDto createOffer(LoanStatementRequestDto request, Boolean isInsuranceEnabled, Boolean isSalaryClient) {

        BigDecimal rate = calculateRate(isInsuranceEnabled, isSalaryClient);

        BigDecimal monthlyPayment = calculateMonthlyPayment(request.getAmount(), request.getTerm(), rate, isInsuranceEnabled);

        BigDecimal totalAmount = calculateTotalAmount(monthlyPayment,request.getTerm());

        return LoanOfferDto.builder()
                .statementId(UUID.randomUUID())
                .requestedAmount(request.getAmount().setScale(RESULT_SCALE, RoundingMode.HALF_UP))
                .totalAmount(totalAmount.setScale(RESULT_SCALE, RoundingMode.HALF_UP))
                .term(request.getTerm())
                .monthlyPayment(monthlyPayment.setScale(RESULT_SCALE, RoundingMode.HALF_UP))
                .rate(rate.setScale(RESULT_SCALE, RoundingMode.HALF_UP))
                .isInsuranceEnabled(isInsuranceEnabled)
                .isSalaryClient(isSalaryClient)
                .build();
    }

    /**
     * Расчет процентной ставки с учетом скидок за страховку и зарплатного клиента.
     * <p>
     * Базовая ставка берется из конфигурации и уменьшается на:
     * <ul>
     *   <li>Скидку за страховку (при наличии)</li>
     *   <li>Скидку за зарплатного клиента (при наличии)</li>
     * </ul>
     *
     * @param isInsuranceEnabled флаг наличия страховки
     * @param isSalaryClient флаг зарплатного клиента
     * @return итоговая процентная ставка
     */
    private BigDecimal calculateRate(Boolean isInsuranceEnabled, Boolean isSalaryClient) {
        BigDecimal rate = calculatorProperties.getBaseRate();

        if (isInsuranceEnabled) {
            rate = rate.subtract(calculatorProperties.getInsuranceRateDiscount());
        }

        if (isSalaryClient) {
            rate = rate.subtract(calculatorProperties.getSalaryClientDiscount());
        }

        rate = rate.setScale(2, RoundingMode.HALF_UP);

        log.debug("Предварительная процентная ставка с учетом скидок за страховку и флага зарплатного клиента = {}%", rate);

        return rate;
    }

    /**
     * Расчет ежемесячного аннуитетного платежа.
     * <p>
     * Формула расчета:
     * Платеж = Сумма × (мес_ставка × (1 + мес_ставка)^срок) / ((1 + мес_ставка)^срок - 1)
     * При страховании кредита, к платежу прибавляется сумма оплаты страховки
     *
     * @param amount итоговая сумма кредита
     * @param term срок кредита в месяцах
     * @param rate годовая процентная ставка
     * @param isInsuranceEnabled флаг наличия страховки
     * @return сумма ежемесячного платежа
     */
    private BigDecimal calculateMonthlyPayment(BigDecimal amount, Integer term, BigDecimal rate, Boolean isInsuranceEnabled) {

        BigDecimal monthlyRate = rate
                .divide(PERCENT_DIVISOR, CALC_SCALE, RoundingMode.HALF_UP)
                .divide(MONTHS_IN_YEAR, CALC_SCALE, RoundingMode.HALF_UP);

        BigDecimal onePlusRate = BigDecimal.ONE.add(monthlyRate);

        BigDecimal annuityRatio = monthlyRate.multiply(onePlusRate.pow(term))
                .divide(onePlusRate.pow(term).subtract(BigDecimal.ONE), CALC_SCALE, RoundingMode.HALF_UP);

        BigDecimal monthlyPayment = amount.multiply(annuityRatio);

        if (isInsuranceEnabled) {
            BigDecimal insurancePrice = amount.multiply(calculatorProperties.getInsuranceCostPercent().divide(PERCENT_DIVISOR, CALC_SCALE, RoundingMode.HALF_UP));
            monthlyPayment = monthlyPayment.add(insurancePrice.divide(new BigDecimal(term),CALC_SCALE,RoundingMode.HALF_UP));
        }

        log.debug("Предварительный расчет ежемесячного аннуитетного платежа = {} руб.", monthlyPayment);

        return monthlyPayment;
    }

    /**
     * Расчет итоговой суммы кредита с учетом страховки.
     * <p>
     * Ежемесячный платеж умножается на количество запланированных платежей
     *
     * @param monthlyPayment сумма месячного платежа
     * @param term срок кредита в месяцах/ количество планируемых платежей
     * @return итоговая сумма кредита (с учетом страховки)
     */
    private BigDecimal calculateTotalAmount(BigDecimal monthlyPayment, Integer term) {
        BigDecimal totalAmount = monthlyPayment.multiply(new BigDecimal(term)).setScale(CALC_SCALE, RoundingMode.HALF_UP);

        log.debug("Предварительная стоимость кредита с учетом страховки = {}", totalAmount);
        return totalAmount;
    }

    /**
     * Валидация даты рождения клиента.
     * <p>
     * Проверяет, что клиент достиг 18-летнего возраста на момент подачи заявки.
     *
     * @param request базовые данные заявки
     * @throws NotValidBirthDateException если возраст менее 18 лет
     */
    private void isValidBirthDate(LoanStatementRequestDto request){
        if(LocalDate.now().minusYears(18).isBefore(request.getBirthDate())){
            throw new NotValidBirthDateException("Неверная дата рождения, дата рождения должна быть ранее 18 лет от текущей даты");
        }
    }
}
