package org.neoflex.deal.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.deal.service.DocumentService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/deal/document")
public class DocumentController {

    private final DocumentService documentService;

    @Operation(summary = "Запрос на отправку документов")
    @PostMapping("/{statementId}/send")
    public void sendDocuments(@PathVariable UUID statementId) {
        documentService.sendDocuments(statementId);
    }

    @Operation(summary = "Запрос на подписание документов")
    @PostMapping("/{statementId}/sign")
    public void signDocuments(@PathVariable UUID statementId) {
        documentService.signDocuments(statementId);
    }

    @Operation(summary = "Подписание документов")
    @PostMapping("/{statementId}/code")
    public void verifyCode(@PathVariable UUID statementId, @RequestBody String code) {
        documentService.verifyCode(statementId, code);
    }
}
