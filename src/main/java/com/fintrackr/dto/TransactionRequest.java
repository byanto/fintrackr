package com.fintrackr.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionRequest {
    private final Long productId; 
    private final String type;
    private final int quantity;
}
