package com.storrego.catalog.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class BulkCreateResponse {

    int total;
    int created;
    int skipped;
    List<BulkError> errors;

    @Value
    @Builder
    public static class BulkError {
        int index;
        String id;
        String reason;
    }
}
