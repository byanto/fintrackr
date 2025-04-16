package com.fintrackr.dto;

import com.fintrackr.model.TransactionType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionResponse {
    private final Long id;
    private final TransactionType type;
    private final int quantity;
    private final Long productId;
}
    
