package org.neoflex.dossier.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Theme {
    FINISH_REGISTRATION("finish-registration","Завершение регистрации - Кредитная заявка"),
    CREATE_DOCUMENTS("create-documents", "Создание документов - Кредитная заявка"),
    SEND_DOCUMENTS("send-documents", "Отправка документов - Кредитная заявка"),
    SEND_SES("send-ses", "Подписание документов - Кредитная заявка"),
    CREDIT_ISSUED("credit-issued", "Кредит одобрен - Поздравляем!"),
    STATEMENT_DENIED("statement-denied", "Решение по кредитной заявке");

    private final String templateName;
    private final String subject;
}
