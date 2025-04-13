package com.fintrackr.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionResponse {
    private final Long id;
    private final String type;
    private final int quantity;
    private final LocalDateTime timestamp;
    private final Long productId;
    private final String productName;
}
    
