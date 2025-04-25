package com.narciso.tedtalks.imports.domain;

import com.narciso.tedtalks.common.errors.ImportError;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ImportResult {
    private int importedCount;
    private List<ImportError> errors;
}
