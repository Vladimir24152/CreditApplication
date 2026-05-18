package org.neoflex.deal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.neoflex.credit.lib.exception.HttpErrorInternalServiceResponse;
import org.neoflex.deal.dto.StatementResponseDto;
import org.neoflex.deal.service.StatementService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/deal/admin/statement")
@RequiredArgsConstructor
@Tag(name = "Admin statement Management", description = "API для управления управления заявками")
public class StatementController {

    private final StatementService statementService;

    @Operation(summary = "Запрос на получения заявки по id")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное получение заявки",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StatementResponseDto.class)
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
    public StatementResponseDto get(@PathVariable UUID statementId){
        return statementService.get(statementId);
    }

    @Operation(summary = "Запрос на получения всех заявок")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешное получение заявок",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = StatementResponseDto.class))
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
    @GetMapping
    public List<StatementResponseDto> getAll(){
        return statementService.getAll();
    }
}
