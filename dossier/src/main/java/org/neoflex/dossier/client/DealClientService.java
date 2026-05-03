package org.neoflex.dossier.client;

import lombok.RequiredArgsConstructor;
import org.neoflex.dossier.dto.DealDocumentDto;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DealClientService {

    private final DealClient dealClient;

    public DealDocumentDto getDealDocument(UUID statementId){
        return dealClient.getDealDocument(statementId);
    }

}
