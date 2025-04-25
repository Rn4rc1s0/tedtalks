package com.narciso.tedtalks.imports.service;

import com.narciso.tedtalks.common.errors.ErrorType;
import com.narciso.tedtalks.common.errors.ImportError;
import com.narciso.tedtalks.imports.domain.ImportResult;
import com.narciso.tedtalks.imports.dto.CsvRecord;
import com.narciso.tedtalks.speakers.domain.Speaker;
import com.narciso.tedtalks.speakers.service.SpeakerService;
import com.narciso.tedtalks.talks.service.TalkService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImportServiceTest {

    @Mock
    private SpeakerService speakerService;

    @Mock
    private TalkService talkService;

    @InjectMocks
    private ImportService importService;

    @Captor
    private ArgumentCaptor<CsvRecord> csvRecordCaptor;
    @Captor
    private ArgumentCaptor<Long> speakerIdCaptor;
    @Captor
    private ArgumentCaptor<Integer> viewsCaptor;
    @Captor
    private ArgumentCaptor<Integer> likesCaptor;

    private Speaker speakerJohn;
    private Speaker speakerJane;

    @BeforeEach
    void setUp() {
        speakerJohn = new Speaker();
        speakerJohn.setId(1L);
        speakerJohn.setName("John Doe");

        speakerJane = new Speaker();
        speakerJane.setId(2L);
        speakerJane.setName("Jane Smith");
    }

    private MockMultipartFile createMockCsvFile(String content) {
        return new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }

    @Test
    @DisplayName("Import successful when CSV is valid")
    void importValidCsv() throws IOException {
        String csvContent = """
            author,title,views,likes,date,link
            John Doe,Talk 1,100,50,January 2023,http://example.com/1
            Jane Smith,Talk 2,200,100,February 2023,http://example.com/2
            """;
        MultipartFile file = createMockCsvFile(csvContent);

        when(speakerService.findOrCreate("John Doe")).thenReturn(speakerJohn);
        when(speakerService.findOrCreate("Jane Smith")).thenReturn(speakerJane);
        doNothing().when(talkService).saveIfNotExists(any(CsvRecord.class), anyLong(), anyInt(), anyInt());

        ImportResult result = importService.importCsv(file);

        assertThat(result.getImportedCount()).isEqualTo(2);
        assertThat(result.getErrors()).isEmpty();

        verify(talkService, times(2)).saveIfNotExists(
                csvRecordCaptor.capture(),
                speakerIdCaptor.capture(),
                viewsCaptor.capture(),
                likesCaptor.capture()
        );

        assertThat(csvRecordCaptor.getAllValues().get(0).getAuthor()).isEqualTo("John Doe");
        assertThat(speakerIdCaptor.getAllValues().get(0)).isEqualTo(1L);
        assertThat(viewsCaptor.getAllValues().get(0)).isEqualTo(100);
        assertThat(likesCaptor.getAllValues().get(0)).isEqualTo(50);

        assertThat(csvRecordCaptor.getAllValues().get(1).getAuthor()).isEqualTo("Jane Smith");
        assertThat(speakerIdCaptor.getAllValues().get(1)).isEqualTo(2L);
        assertThat(viewsCaptor.getAllValues().get(1)).isEqualTo(200);
        assertThat(likesCaptor.getAllValues().get(1)).isEqualTo(100);

        verify(speakerService, times(1)).findOrCreate("John Doe");
        verify(speakerService, times(1)).findOrCreate("Jane Smith");
    }

    @Test
    @DisplayName("Import fails with invalid numeric fields")
    void importCsvWithInvalidNumbers() throws IOException {
        String csvContent = """
            author,title,views,likes,date,link
            John Doe,Talk 1,invalid,50,January 2023,http://example.com/1
            Jane Smith,Talk 2,200,NaN,February 2023,http://example.com/2
            """;
        MultipartFile file = createMockCsvFile(csvContent);

        ImportResult result = importService.importCsv(file);

        assertThat(result.getImportedCount()).isEqualTo(0);
        assertThat(result.getErrors()).hasSize(2);
        assertThat(result.getErrors()).containsExactlyInAnyOrder(
                new ImportError(2, ErrorType.NUMBER_PARSE_ERROR, "views", "invalid", "Invalid format for 'views': 'invalid'. Only digits are allowed."),
                new ImportError(3, ErrorType.NUMBER_PARSE_ERROR, "likes", "NaN", "Invalid format for 'likes': 'NaN'. Only digits are allowed.")
        );

        verifyNoInteractions(talkService);
        verifyNoInteractions(speakerService);
    }

    @Test
    @DisplayName("Import fails with invalid date format")
    void importCsvWithInvalidDates() throws IOException {
        String csvContent = """
            author,title,views,likes,date,link
            John Doe,Talk 1,100,50,2023-01-01,http://example.com/1
            Jane Smith,Talk 2,200,100,invalid date,http://example.com/2
            """;
        MultipartFile file = createMockCsvFile(csvContent);

        ImportResult result = importService.importCsv(file);

        assertThat(result.getImportedCount()).isEqualTo(0);
        assertThat(result.getErrors()).hasSize(2);
        assertThat(result.getErrors()).containsExactlyInAnyOrder(
                new ImportError(2, ErrorType.DATE_PARSE_ERROR, "date", "2023-01-01", "Cannot parse '2023-01-01' as 'MMMM yyyy'."),
                new ImportError(3, ErrorType.DATE_PARSE_ERROR, "date", "invalid date", "Cannot parse 'invalid date' as 'MMMM yyyy'.")
        );

        verifyNoInteractions(talkService);
        verifyNoInteractions(speakerService);
    }

    @Test
    @DisplayName("Import handles database errors gracefully during speaker lookup")
    void importCsvWithDatabaseErrorOnSpeaker() throws IOException {
        String csvContent = """
            author,title,views,likes,date,link
            John Doe,Talk 1,100,50,January 2023,http://example.com/1
            """;
        MultipartFile file = createMockCsvFile(csvContent);

        RuntimeException dbException = new RuntimeException("Database error during speaker lookup");
        when(speakerService.findOrCreate("John Doe")).thenThrow(dbException);

        ImportResult result = importService.importCsv(file);

        assertThat(result.getImportedCount()).isEqualTo(0);
        assertThat(result.getErrors()).hasSize(1);
        ImportError error = result.getErrors().get(0);
        assertThat(error.getType()).isEqualTo(ErrorType.GENERIC_ERROR);
        assertThat(error.getMessage()).isEqualTo("Error saving record for title 'Talk 1': Database error during speaker lookup");

        verify(speakerService, times(1)).findOrCreate("John Doe");
        verifyNoInteractions(talkService);
    }

    @Test
    @DisplayName("Import handles database errors gracefully during talk save")
    void importCsvWithDatabaseErrorOnTalkSave() throws IOException {
        String csvContent = """
            author,title,views,likes,date,link
            John Doe,Talk 1,100,50,January 2023,http://example.com/1
            """;
        MultipartFile file = createMockCsvFile(csvContent);

        when(speakerService.findOrCreate("John Doe")).thenReturn(speakerJohn);
        RuntimeException dbException = new RuntimeException("Database error during talk save");
        doThrow(dbException).when(talkService).saveIfNotExists(any(CsvRecord.class), eq(1L), eq(100), eq(50));

        ImportResult result = importService.importCsv(file);

        assertThat(result.getImportedCount()).isEqualTo(0);
        assertThat(result.getErrors()).hasSize(1);
        ImportError error = result.getErrors().get(0);
        assertThat(error.getType()).isEqualTo(ErrorType.GENERIC_ERROR);
        assertThat(error.getMessage()).isEqualTo("Error saving record for title 'Talk 1': Database error during talk save");

        verify(speakerService, times(1)).findOrCreate("John Doe");
        verify(talkService, times(1)).saveIfNotExists(any(CsvRecord.class), eq(1L), eq(100), eq(50)); // Verify the call was made
    }
}