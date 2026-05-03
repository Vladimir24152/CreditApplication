package org.neoflex.dossier.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.creditapplicationsupportstarter.dto.EmailMessage;
import org.neoflex.creditapplicationsupportstarter.enums.Theme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmail(EmailMessage message) {
        String subject = getSubject(message.getTheme());
        String content = buildEmailContent(message);

        sendSimpleEmail(message.getAddress(), subject, content);
    }

    public void sendEmailWithDocument(String to, String subject, String text, byte[] attachment, String fileName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);

            if (attachment != null && attachment.length > 0) {
                helper.addAttachment(fileName, new ByteArrayResource(attachment), "application/pdf");
            }

            mailSender.send(message);
            log.info("Письмо с вложением отправлено на адрес: {}, тема: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Не удалось отправить письмо с вложением: {}", e.getMessage(), e);
        }
    }

    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);

            mailSender.send(message);
            log.info("Электронное письмо отправлено по адресу: {}, тема сообщения: {}", to, subject);

        } catch (MessagingException e) {
            log.error("Не удалось отправить электронное письмо по адресу: {},  ошибка: {}", to, e.getMessage(), e);
        }
    }

    private String getSubject(Theme theme) {
        return switch (theme) {
            case FINISH_REGISTRATION -> "Завершение регистрации - Кредитная заявка";
            case CREATE_DOCUMENTS -> "Создание документов - Кредитная заявка";
            case SEND_DOCUMENTS -> "Отправка документов - Кредитная заявка";
            case SEND_SES -> "Подписание документов - Кредитная заявка";
            case CREDIT_ISSUED -> "Кредит одобрен - Поздравляем!";
            case STATEMENT_DENIED -> "Решение по кредитной заявке";
        };
    }

    private String buildEmailContent(EmailMessage message) {
        UUID statementId = message.getStatementId();
        String text = message.getText();

        String htmlHeader = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #4CAF50; color: white; padding: 10px; text-align: center; }
                    .content { padding: 20px; border: 1px solid #ddd; }
                    .footer { font-size: 12px; color: #999; text-align: center; margin-top: 20px; }
                    .button { background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; }
                </style>
            </head>
            <body>
            """;

        String htmlFooter = """
                    <div class="footer">
                        <p>Это автоматическое сообщение, пожалуйста, не отвечайте на него.</p>
                        <p>© 2024 Кредитная система</p>
                    </div>
                </div>
            </body>
            </html>
            """;

        String body = switch (message.getTheme()) {
            case FINISH_REGISTRATION -> String.format("""
                <div class="container">
                    <div class="header"><h2>Завершение регистрации</h2></div>
                    <div class="content">
                        <p>Уважаемый клиент!</p>
                        <p>%s</p>
                        <p>Номер вашей заявки: <strong>%s</strong></p>
                        <p>С уважением,<br>Самый лучший в мире банк</p>
                    </div>
                </div>
                """, text, statementId, statementId);

            case CREATE_DOCUMENTS -> String.format("""
                <div class="container">
                    <div class="header"><h2>Документы созданы</h2></div>
                    <div class="content">
                        <p>Уважаемый клиент!</p>
                        <p>%s</p>
                        <p>Номер заявки: <strong>%s</strong></p>
                        <p><a href="http://deal:8081/api/v1/deal/document/%s/send">Сформировать документы</a></p>
                        <p>С уважением,<br>Команда кредитного отдела</p>
                    </div>
                </div>
                """, text, statementId, statementId);

            case SEND_DOCUMENTS -> String.format("""
                <div class="container">
                    <div class="header"><h2>Документы отправлены</h2></div>
                    <div class="content">
                        <p>Уважаемый клиент!</p>
                        <p>%s</p>
                        <p>Номер заявки: <strong>%s</strong></p>
                        <p><a href="http://deal:8081/api/v1/deal/document/%s/sign">Подписать документы</a></p>
                        <p>С уважением,<br>Команда кредитного отдела</p>
                    </div>
                </div>
                """, text, statementId, statementId);

            case SEND_SES -> String.format("""
                <div class="container">
                    <div class="header"><h2>Код подтверждения</h2></div>
                    <div class="content">
                        <p>Уважаемый клиент!</p>
                        <p>%s</p>
                        <p>Номер заявки: <strong>%s</strong></p>
                        <p><strong>Введите код: %s</strong></p>
                        <p><a href="http://deal:8081/api/v1/deal/document/%s/code">Подтвердить</a></p>
                        <p>С уважением,<br>Команда кредитного отдела</p>
                    </div>
                </div>
                """, text, statementId, text, statementId);

            case CREDIT_ISSUED -> String.format("""
                <div class="container">
                    <div class="header"><h2 style="background-color: #4CAF50;">Кредит одобрен!</h2></div>
                    <div class="content">
                        <p>Уважаемый клиент!</p>
                        <p>Поздравляем! Ваша кредитная заявка <strong>№%s</strong> одобрена.</p>
                        <p>%s</p>
                        <p>Деньги будут перечислены в течение 24 часов на указанный вами счёт.</p>
                        <p>С уважением,<br>Команда кредитного отдела</p>
                    </div>
                </div>
                """, statementId, text);

            case STATEMENT_DENIED -> String.format("""
                <div class="container">
                    <div class="header"><h2 style="background-color: #f44336;">Решение по заявке</h2></div>
                    <div class="content">
                        <p>Уважаемый клиент!</p>
                        <p>%s</p>
                        <p>Номер заявки: <strong>%s</strong></p>
                        <p>Если у вас есть вопросы, пожалуйста, свяжитесь с нашим контакт-центром.</p>
                        <p>С уважением,<br>Команда кредитного отдела</p>
                    </div>
                </div>
                """, text, statementId);
        };

        return htmlHeader + body + htmlFooter;
    }
}