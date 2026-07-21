package edu.cit.estillore.MentorMatch.MentorMatch.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * Replaces a tutor's entire weekly availability in one call — simpler for
 * the client than diffing individual slot add/remove operations (FR-003).
 */
@Data
public class AvailabilityUpdateRequest {

    @NotNull
    @Valid
    private List<AvailabilitySlotRequest> slots;
}
