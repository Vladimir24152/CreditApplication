package org.neoflex.dossier.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.neoflex.dossier.dto.EmailMessage;
import org.neoflex.dossier.enums.Theme;
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
    private final BuildEmailContentService buildEmailContentService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmail(EmailMessage message) {
        String subject = getSubject(message.getTheme());
        String content = buildEmailContentService.buildEmailContent(message);

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
}