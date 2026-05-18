package org.neoflex.dossier.client;

import org.neoflex.dossier.dto.DealDocumentDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.UUID;

@HttpExchange("${deal.endpoints.base}")
public interface DealClient {

    @GetExchange("/{statementId}")
    DealDocumentDto getDealDocument(@PathVariable UUID statementId);
}
