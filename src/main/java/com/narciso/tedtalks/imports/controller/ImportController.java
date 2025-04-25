package com.narciso.tedtalks.imports.controller;

import com.narciso.tedtalks.imports.domain.ImportResult;
import com.narciso.tedtalks.imports.service.ImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class ImportController {
    private final ImportService importService;

    @PostMapping("/talks")
    public ResponseEntity<ImportResult> importTalks(@RequestParam("file") MultipartFile file) {
        try {
            ImportResult result = importService.importCsv(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ImportResult(0, List.of())
            );
        }
    }
}