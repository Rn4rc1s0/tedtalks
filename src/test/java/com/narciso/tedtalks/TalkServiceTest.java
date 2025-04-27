package com.narciso.tedtalks.talks.service;

import com.narciso.tedtalks.common.exception.ResourceNotFoundException;
import com.narciso.tedtalks.speakers.domain.Speaker;
import com.narciso.tedtalks.speakers.service.SpeakerService;
import com.narciso.tedtalks.talks.dao.TalkDao;
import com.narciso.tedtalks.talks.domain.Talk;
import com.narciso.tedtalks.talks.dto.CreateTalkDto;
import com.narciso.tedtalks.talks.dto.TalkDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TalkServiceTest {

    @Mock
    private TalkDao talkDao;

    @Mock
    private SpeakerService speakerService;

    @InjectMocks
    private TalkService talkService;

    private static final DateTimeFormatter CREATE_DTO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private TalkDto sampleTalkDto1;
    private TalkDto sampleTalkDto2;
    private Speaker sampleSpeaker;

    @BeforeEach
    void setup() {
        sampleSpeaker = new Speaker();
        sampleSpeaker.setId(1L);
        sampleSpeaker.setName("Test Speaker");

        sampleTalkDto1 = TalkDto.builder()
                .id(1L)
                .title("Talk 1")
                .speakerId(sampleSpeaker.getId())
                .speakerName(sampleSpeaker.getName())
                .date(YearMonth.of(2023, 1))
                .views(1000)
                .likes(100)
                .link("http://example.com/1")
                .build();

        sampleTalkDto2 = TalkDto.builder()
                .id(2L)
                .title("Talk 2")
                .speakerId(sampleSpeaker.getId())
                .speakerName(sampleSpeaker.getName())
                .date(YearMonth.of(2023, 2))
                .views(2000)
                .likes(200)
                .link("http://example.com/2")
                .build();
    }

    @Test
    @DisplayName("Should create talk when speakerId is provided and valid")
    void create_withValidSpeakerId() {
        // Arrange
        CreateTalkDto dto = new CreateTalkDto();
        dto.setTitle("New Talk via ID");
        dto.setSpeakerId(sampleSpeaker.getId());
        dto.setDate("2023-05");
        dto.setViews(100);
        dto.setLikes(50);
        dto.setLink("http://example.com/new");

        when(speakerService.findById(sampleSpeaker.getId())).thenReturn(sampleSpeaker);

        Talk talkToCreate = Talk.builder()
                .title(dto.getTitle())
                .date(YearMonth.parse(dto.getDate(), CREATE_DTO_DATE_FORMATTER))
                .views(dto.getViews())
                .likes(dto.getLikes())
                .link(dto.getLink())
                .speakerId(sampleSpeaker.getId())
                .build();
        Talk createdTalkDomain = Talk.builder()
                                     .id(99L)
                                     .speakerId(
                                        sampleSpeaker.getId())
                                                     .title(dto.getTitle())
                                                     .date(YearMonth.parse(dto.getDate(), CREATE_DTO_DATE_FORMATTER))
                                                     .build();
        when(talkDao.create(any(Talk.class))).thenReturn(createdTalkDomain);

        TalkDto expectedFinalDto = TalkDto.builder()
                .id(createdTalkDomain.getId())
                .title(createdTalkDomain.getTitle())
                .speakerId(sampleSpeaker.getId())
                .speakerName(sampleSpeaker.getName())
                .date(createdTalkDomain.getDate())
                .views(dto.getViews())
                .likes(dto.getLikes())
                .link(dto.getLink())
                .build();
        when(talkDao.findById(createdTalkDomain.getId())).thenReturn(Optional.of(expectedFinalDto));

        TalkDto result = talkService.create(dto);

        assertThat(result).isEqualTo(expectedFinalDto);

        verify(speakerService).findById(sampleSpeaker.getId());
        verify(speakerService, never()).findOrCreate(anyString());
        verify(talkDao).create(any(Talk.class));
        verify(talkDao).findById(createdTalkDomain.getId());
    }

    @Test
    @DisplayName("Should create talk when speakerName is provided")
    void create_withSpeakerName() {
        CreateTalkDto dto = new CreateTalkDto();
        dto.setTitle("New Talk via Name");
        dto.setSpeakerName(sampleSpeaker.getName());
        dto.setDate("2023-06");
        dto.setViews(200);
        dto.setLikes(20);
        dto.setLink("http://example.com/new2");

        when(speakerService.findOrCreate(sampleSpeaker.getName())).thenReturn(sampleSpeaker);

        Talk createdTalkDomain = Talk.builder().id(100L).speakerId(sampleSpeaker.getId()).title(dto.getTitle()).date(YearMonth.parse(dto.getDate(), CREATE_DTO_DATE_FORMATTER)).build();
        when(talkDao.create(any(Talk.class))).thenReturn(createdTalkDomain);

        TalkDto expectedFinalDto = TalkDto.builder()
                .id(createdTalkDomain.getId())
                .title(createdTalkDomain.getTitle())
                .speakerId(sampleSpeaker.getId())
                .speakerName(sampleSpeaker.getName())
                .date(createdTalkDomain.getDate())
                .views(dto.getViews())
                .likes(dto.getLikes())
                .link(dto.getLink())
                .build();
        when(talkDao.findById(createdTalkDomain.getId())).thenReturn(Optional.of(expectedFinalDto));

        TalkDto result = talkService.create(dto);

        assertThat(result).isEqualTo(expectedFinalDto);

        verify(speakerService, never()).findById(anyLong());
        verify(speakerService).findOrCreate(sampleSpeaker.getName());
        verify(talkDao).create(any(Talk.class));
        verify(talkDao).findById(createdTalkDomain.getId());
    }

    @Test
    @DisplayName("Should throw exception when creating talk and provided speakerId is invalid")
    void create_withInvalidSpeakerId() {
        CreateTalkDto dto = new CreateTalkDto();
        dto.setTitle("New Talk Invalid ID");
        dto.setSpeakerId(999L);
        dto.setDate("2023-07");
        dto.setViews(10);
        dto.setLikes(1);

        when(speakerService.findById(999L)).thenThrow(new ResourceNotFoundException("Speaker not found"));

        assertThatThrownBy(() -> talkService.create(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Speaker not found with provided speakerId: 999");

        verify(speakerService).findById(999L);
        verifyNoInteractions(talkDao);
    }

    @Test
    @DisplayName("Should throw exception when creating talk and neither speakerId nor speakerName is provided")
    void create_withoutSpeakerIdAndName() {
        CreateTalkDto dto = new CreateTalkDto();
        dto.setTitle("New Talk No Speaker");
        dto.setDate("2023-08");
        dto.setViews(5);
        dto.setLikes(0);

        assertThatThrownBy(() -> talkService.create(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Either speakerId or a non-blank speakerName must be provided to create a talk.");

        verifyNoInteractions(speakerService);
        verifyNoInteractions(talkDao);
    }

    @Test
    @DisplayName("Should throw exception when creating talk with invalid date format")
    void create_withInvalidDateFormat() {
        CreateTalkDto dto = new CreateTalkDto();
        dto.setTitle("New Talk Invalid Date");
        dto.setSpeakerName("Some Speaker");
        dto.setDate("09/2023");
        dto.setViews(1);
        dto.setLikes(1);

        when(speakerService.findOrCreate("Some Speaker")).thenReturn(sampleSpeaker);


        assertThatThrownBy(() -> talkService.create(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid date format for date: '09/2023'. Expected format: yyyy-MM.");

        verify(speakerService).findOrCreate("Some Speaker");
        verifyNoInteractions(talkDao);
    }

    @Test
    @DisplayName("Should find talk DTO by ID when exists")
    void findById_WhenExists() {
        Long id = 1L;
        when(talkDao.findById(id)).thenReturn(Optional.of(sampleTalkDto1));

        TalkDto result = talkService.findById(id);

        assertThat(result).isEqualTo(sampleTalkDto1);
        verify(talkDao).findById(id);
    }

    @Test
    @DisplayName("Should throw exception when talk not found by ID")
    void findById_WhenNotExists() {
        Long id = 99L;
        when(talkDao.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> talkService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Talk not found with ID: " + id);

        verify(talkDao).findById(id);
    }

    @Test
    @DisplayName("Should return all talk DTOs paginated")
    void findAll() {
        Pageable pageable = PageRequest.of(0, 10);
        List<TalkDto> talkDtos = List.of(sampleTalkDto1, sampleTalkDto2);
        Page<TalkDto> expectedPage = new PageImpl<>(talkDtos, pageable, talkDtos.size());
        when(talkDao.findAll(pageable)).thenReturn(expectedPage);

        Page<TalkDto> result = talkService.findAll(pageable);

        assertThat(result).isEqualTo(expectedPage);
        verify(talkDao).findAll(pageable);
    }


    @Test
    @DisplayName("Should delete existing talk")
    void delete_WhenExists() {
        Long id = 1L;
        when(talkDao.findById(id)).thenReturn(Optional.of(sampleTalkDto1));
        doNothing().when(talkDao).delete(id);

        talkService.delete(id);
        verify(talkDao).findById(id);
        verify(talkDao).delete(id);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existing talk")
    void delete_WhenNotExists() {
        Long id = 99L;
        when(talkDao.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> talkService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Talk not found with ID: " + id);

        verify(talkDao).findById(id);
        verify(talkDao, never()).delete(anyLong());
    }

    @Test
    @DisplayName("Should update existing talk")
    void update_WhenExists() {
        Long id = 1L;
        Talk updateInput = Talk.builder()
                .title("Updated Title")
                .date(YearMonth.of(2024, 1))
                .views(1500)
                .likes(150)
                .link("http://example.com/updated")
                .speakerId(sampleSpeaker.getId())
                .build();

        when(talkDao.findById(id)).thenReturn(Optional.of(sampleTalkDto1));

        when(speakerService.findById(updateInput.getSpeakerId())).thenReturn(sampleSpeaker);

        doNothing().when(talkDao).update(any(Talk.class));

        TalkDto finalUpdatedDto = TalkDto.builder()
                .id(id)
                .title(updateInput.getTitle())
                .date(updateInput.getDate())
                .views(updateInput.getViews())
                .likes(updateInput.getLikes())
                .link(updateInput.getLink())
                .speakerId(updateInput.getSpeakerId())
                .speakerName(sampleSpeaker.getName())
                .build();
        when(talkDao.findById(id))
                .thenReturn(Optional.of(sampleTalkDto1))
                .thenReturn(Optional.of(finalUpdatedDto));

        TalkDto result = talkService.update(id, updateInput);

        assertThat(result).isEqualTo(finalUpdatedDto);

        verify(talkDao, times(2)).findById(id);
        verify(speakerService).findById(updateInput.getSpeakerId());
        verify(talkDao).update(any(Talk.class));
    }

    @Test
    @DisplayName("Should throw exception when updating non-existing talk")
    void update_WhenNotExists() {
        Long id = 99L;
        Talk updateInput = Talk.builder().speakerId(1L).title("Any").date(YearMonth.now()).build();

        when(talkDao.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> talkService.update(id, updateInput))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Talk not found with ID: " + id);

        verify(talkDao).findById(id);
        verify(speakerService, never()).findById(anyLong());
        verify(talkDao, never()).update(any(Talk.class));
    }

    @Test
    @DisplayName("Should throw exception when updating talk with invalid speakerId")
    void update_WithInvalidSpeakerId() {
        Long id = 1L;
        Long invalidSpeakerId = 999L;
        Talk updateInput = Talk.builder()
                .title("Updated Title")
                .date(YearMonth.of(2024, 1))
                .views(1500)
                .likes(150)
                .link("http://example.com/updated")
                .speakerId(invalidSpeakerId)
                .build();

        when(talkDao.findById(id)).thenReturn(Optional.of(sampleTalkDto1));

        when(speakerService.findById(invalidSpeakerId)).thenThrow(new ResourceNotFoundException("Speaker not found"));

        assertThatThrownBy(() -> talkService.update(id, updateInput))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Speaker not found");

        verify(talkDao).findById(id);
        verify(speakerService).findById(invalidSpeakerId);
        verify(talkDao, never()).update(any(Talk.class));
    }

}