package org.neoflex.deal.model.jsonb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.neoflex.deal.model.enums.ChangeType;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusHistory {

    private String status;

    private LocalDateTime time;

    private ChangeType changeType;
}
