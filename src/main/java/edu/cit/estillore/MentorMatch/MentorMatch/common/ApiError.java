package edu.cit.estillore.MentorMatch.MentorMatch.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ApiError {
    private String message;
    private Map<String, String> fieldErrors; // null when not a field-validation error
}
