package com.fintrackr.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {
    private final Long id;
    private final String name; 
    private final int stock;
}
