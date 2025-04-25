package com.narciso.tedtalks.imports.service;

import com.narciso.tedtalks.common.errors.ErrorType;
import com.narciso.tedtalks.common.errors.ImportError;
import com.narciso.tedtalks.imports.domain.ImportResult;
import com.narciso.tedtalks.imports.dto.CsvRecord;
import com.narciso.tedtalks.speakers.service.SpeakerService;
import com.narciso.tedtalks.talks.service.TalkService;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ImportService {
    private final SpeakerService speakerService;
    private final TalkService talkService;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    public ImportResult importCsv(MultipartFile file) throws IOException {
        List<ImportError> errors = new ArrayList<>();
        int successCount = 0;

        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CsvToBean<CsvRecord> csv = new CsvToBeanBuilder<CsvRecord>(reader)
                    .withType(CsvRecord.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withThrowExceptions(false)
                    .build();

            for (CsvException ex : csv.getCapturedExceptions()) {
                int line = Math.toIntExact(ex.getLineNumber());
                ErrorType type = ex instanceof CsvRequiredFieldEmptyException
                        ? ErrorType.MISSING_FIELD
                        : ErrorType.COLUMN_MISMATCH;
                errors.add(new ImportError(
                        line,
                        type,
                        null,
                        null,
                        ex.getMessage()
                ));
            }

            List<CsvRecord> records;
            try {
                records = csv.parse();
            } catch (RuntimeException e) {
                errors.add(new ImportError(
                        -1,
                        ErrorType.GENERIC_ERROR,
                        null,
                        null,
                        "Critical error during CSV parsing: " + e.getMessage()
                ));
                return new ImportResult(0, errors);
            }


            int currentLine = 1;
            for (CsvRecord rec : records) {
                currentLine++;
                boolean recordHasError = false;

                String viewsRaw = rec.getViews();
                int views = 0;

                if (viewsRaw == null || viewsRaw.trim().isEmpty()) {
                    errors.add(new ImportError(
                            currentLine, ErrorType.MISSING_FIELD, "views", viewsRaw,
                            "Field 'views' cannot be empty."
                    ));
                    recordHasError = true;
                } else if (!viewsRaw.matches("^\\d+$")) {
                    errors.add(new ImportError(
                            currentLine, ErrorType.NUMBER_PARSE_ERROR, "views", viewsRaw,
                            String.format("Invalid format for 'views': '%s'. Only digits are allowed.", viewsRaw)
                    ));
                    recordHasError = true;
                } else {
                    try {
                        views = Integer.parseInt(viewsRaw);
                    } catch (NumberFormatException nfe) {
                        errors.add(new ImportError(
                                currentLine, ErrorType.NUMBER_PARSE_ERROR, "views", viewsRaw,
                                String.format("Value '%s' for 'views' is too large for an integer or cannot be parsed.", viewsRaw)
                        ));
                        recordHasError = true;
                    }
                }

                String likesRaw = rec.getLikes();
                int likes = 0;

                if (likesRaw == null || likesRaw.trim().isEmpty()) {
                    errors.add(new ImportError(
                            currentLine, ErrorType.MISSING_FIELD, "likes", likesRaw,
                            "Field 'likes' cannot be empty."
                    ));
                    recordHasError = true;
                } else if (!likesRaw.matches("^\\d+$")) {
                    errors.add(new ImportError(
                            currentLine, ErrorType.NUMBER_PARSE_ERROR, "likes", likesRaw,
                            String.format("Invalid format for 'likes': '%s'. Only digits are allowed.", likesRaw)
                    ));
                    recordHasError = true;
                } else {
                    try {
                        likes = Integer.parseInt(likesRaw);
                    } catch (NumberFormatException nfe) {
                        errors.add(new ImportError(
                                currentLine, ErrorType.NUMBER_PARSE_ERROR, "likes", likesRaw,
                                String.format("Value '%s' for 'likes' is too large for an integer or cannot be parsed.", likesRaw)
                        ));
                        recordHasError = true;
                    }
                }

                String dateRaw = rec.getDate();
                if (dateRaw == null || dateRaw.trim().isEmpty()) {
                    errors.add(new ImportError(
                            currentLine, ErrorType.MISSING_FIELD, "date", dateRaw,
                            "Field 'date' cannot be empty."
                    ));
                    recordHasError = true;
                } else {
                    try {
                        YearMonth.parse(dateRaw, DATE_FORMATTER);
                    } catch (Exception dpe) {
                        errors.add(new ImportError(
                                currentLine, ErrorType.DATE_PARSE_ERROR, "date", dateRaw,
                                String.format("Cannot parse '%s' as 'MMMM yyyy'.", dateRaw)
                        ));
                        recordHasError = true;
                    }
                }

                if (rec.getAuthor() == null || rec.getAuthor().trim().isEmpty()) {
                    errors.add(new ImportError(
                            currentLine, ErrorType.MISSING_FIELD, "author", rec.getAuthor(),
                            "Field 'author' cannot be empty."
                    ));
                    recordHasError = true;
                }
                if (rec.getTitle() == null || rec.getTitle().trim().isEmpty()) {
                    errors.add(new ImportError(
                            currentLine, ErrorType.MISSING_FIELD, "title", rec.getTitle(),
                            "Field 'title' cannot be empty."
                    ));
                    recordHasError = true;
                }

                if (recordHasError) {
                    continue;
                }

                try {
                    var speaker = speakerService.findOrCreate(rec.getAuthor());
                    talkService.saveIfNotExists(rec, speaker.getId(), views, likes);
                    successCount++;
                } catch (Exception e) {
                    errors.add(new ImportError(
                            currentLine,
                            ErrorType.GENERIC_ERROR,
                            null,
                            null,
                            String.format("Error saving record for title '%s': %s", rec.getTitle(), e.getMessage())
                    ));
                }
            }
        }

        return new ImportResult(successCount, errors);
    }
}