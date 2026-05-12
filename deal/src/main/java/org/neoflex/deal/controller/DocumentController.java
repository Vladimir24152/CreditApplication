package org.neoflex.deal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neoflex.credit.lib.exception.HttpErrorInternalServiceResponse;
import org.neoflex.deal.dto.DealDocumentDto;
import org.neoflex.deal.service.DocumentService;
import org.neoflex.deal.service.StatementService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/deal/document")
public class DocumentController {

    private final DocumentService documentService;
    private final StatementService statementService;

    @Operation(summary = "Запрос на отправку документов")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная отправка документов"
            ),
            @ApiResponse(responseCode = "404",
                    description = "Не найдена заявка с id указанным в запросе",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorInternalServiceResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "500",
                    description = "Внутренняя ошибка сервера при обработке запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorInternalServiceResponse.class)
                    )
            )
    })
    @PostMapping("/{statementId}/send")
    public void sendDocuments(@PathVariable UUID statementId) {
        documentService.sendDocuments(statementId);
    }

    @Operation(summary = "Запрос на подписание документов")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная данных для подписи документов"
            ),
            @ApiResponse(responseCode = "404",
                    description = "Не найдена заявка с id указанным в запросе",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorInternalServiceResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "500",
                    description = "Внутренняя ошибка сервера при обработке запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorInternalServiceResponse.class)
                    )
            )
    })
    @PostMapping("/{statementId}/sign")
    public void signDocuments(@PathVariable UUID statementId) {
        documentService.signDocuments(statementId);
    }

    @Operation(summary = "Подписание документов")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешная подпись документов"
            ),
            @ApiResponse(responseCode = "403",
                    description = "Неверный SES код для заявки",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorInternalServiceResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Не найдена заявка с id указанным в запросе",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorInternalServiceResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "500",
                    description = "Внутренняя ошибка сервера при обработке запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorInternalServiceResponse.class)
                    )
            )
    })
    @PostMapping("/{statementId}/code")
    public void verifyCode(@PathVariable UUID statementId, @RequestParam String code) {
        documentService.verifyCode(statementId, code);
    }

    @Operation(summary = "Получение информации для составления документов для оформления кредита",
            description = "Принимает id заявки на кредит и возвращает информацию для составления документов")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное получение информации по кредиту",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DealDocumentDto.class)
                    )
            ),
            @ApiResponse(responseCode = "404",
                    description = "Не найдена заявка с id указанным в запросе",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorInternalServiceResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "500",
                    description = "Внутренняя ошибка сервера при обработке запроса",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = HttpErrorInternalServiceResponse.class)
                    )
            )
    })
    @GetMapping("/{statementId}")
    public DealDocumentDto getInfo(@PathVariable UUID statementId){
        return statementService.getInfo(statementId);
    }
}
