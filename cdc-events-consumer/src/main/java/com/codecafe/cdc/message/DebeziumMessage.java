package com.codecafe.cdc.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DebeziumMessage<T> {

    private T before;
    private T after;
    private Source source;
    private Operation op;

    @JsonProperty(value = "ts_ms")
    private Long timestampMs;

}