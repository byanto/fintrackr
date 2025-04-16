package com.fintrackr.dto;

import com.fintrackr.model.TransactionType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionRequest {
    private final TransactionType type;
    private final int quantity;
    private final Long productId;
}
