package com.budiyanto.fintrackr.userservice.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard error response format based on RFC 7807 Problem Details.")
public record ErrorResponse(
    @Schema(description = "A URI reference that identifies the problem type.")
    String type,

    @Schema(description = "A short, human-readable summary of the problem type.")
    String title,

    @Schema(description = "The HTTP status code.")
    int status,

    @Schema(description = "A human-readable explanation specific to this occurrence of the problem.")
    String detail,

    @Schema(description = "The timestamp when the error occurred.")
    Instant timestamp
) {}
