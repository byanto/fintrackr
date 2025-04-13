package com.fintrackr.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductRequest {
    private final String name;
    private final int stock;
}
