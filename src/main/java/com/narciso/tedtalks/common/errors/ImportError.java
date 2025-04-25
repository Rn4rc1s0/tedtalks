package com.narciso.tedtalks.common.errors;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImportError {
    private int line;
    private ErrorType type;
    private String field;
    private String value;
    private String message;
}

