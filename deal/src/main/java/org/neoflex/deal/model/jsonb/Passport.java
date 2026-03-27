package org.neoflex.deal.model.jsonb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Passport {

    private String series;

    private String number;

    private String issueBranch;

    private LocalDate issueDate;
}
