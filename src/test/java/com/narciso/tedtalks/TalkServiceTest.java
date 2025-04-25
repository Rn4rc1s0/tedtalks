package com.narciso.tedtalks.talks.service;

import com.narciso.tedtalks.common.exception.ResourceNotFoundException;
import com.narciso.tedtalks.imports.dto.CsvRecord;
import com.narciso.tedtalks.speakers.domain.Speaker;
import com.narciso.tedtalks.speakers.service.SpeakerService;
import com.narciso.tedtalks.talks.dao.TalkDao;
import com.narciso.tedtalks.talks.domain.Talk;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TalkServiceTest {

    @Mock
    private TalkDao talkDao;

    @Mock
    private SpeakerService speakerService;

    @InjectMocks
    private TalkService talkService;

    @Test
    @DisplayName("Should create new talk")
    void create() {
        Talk talk = Talk.builder()
                .title("New Talk")
                .speakerId(1L)
                .date(YearMonth.now())
                .views(100)
                .likes(50)
                .build();
        Talk savedTalk = Talk.builder().id(1L).title("New Talk").build();

        when(speakerService.findById(1L)).thenReturn(mock(Speaker.class));
        when(talkDao.create(talk)).thenReturn(savedTalk);

        Talk result = talkService.create(talk);

        assertThat(result).isEqualTo(savedTalk);
        verify(talkDao).create(talk);
    }

    @Test
    @DisplayName("Should throw exception when creating talk with invalid data")
    void create_WithInvalidData() {
        Talk invalidTalk = Talk.builder().build();

        assertThatThrownBy(() -> talkService.create(invalidTalk))
            .isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(talkDao);
    }

    @Test
    @DisplayName("Should find talk by ID when exists")
    void findById_WhenExists() {
        Long id = 1L;
        Talk talk = Talk.builder().id(id).title("Test Talk").build();
        when(talkDao.findById(id)).thenReturn(Optional.of(talk));

        Talk result = talkService.findById(id);

        assertThat(result).isEqualTo(talk);
        verify(talkDao).findById(id);
    }

    @Test
    @DisplayName("Should throw exception when talk not found by ID")
    void findById_WhenNotExists() {
        Long id = 1L;
        when(talkDao.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> talkService.findById(id))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Talk not found with ID: " + id);

        verify(talkDao).findById(id);
    }

    @Test
    @DisplayName("Should return all talks paginated")
    void findAll() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Talk> talks = List.of(
            Talk.builder().id(1L).title("Talk 1").build(),
            Talk.builder().id(2L).title("Talk 2").build()
        );
        Page<Talk> expectedPage = new PageImpl<>(talks, pageable, talks.size());

        when(talkDao.findAll(pageable)).thenReturn(expectedPage);

        Page<Talk> result = talkService.findAll(pageable);

        assertThat(result).isEqualTo(expectedPage);
        verify(talkDao).findAll(pageable);
    }

    @Test
    @DisplayName("Should delete existing talk")
    void delete_WhenExists() {
        Long id = 1L;
        Talk talk = Talk.builder().id(id).title("Test Talk").build();

        when(talkDao.findById(id)).thenReturn(Optional.of(talk));

        talkService.delete(id);

        verify(talkDao).findById(id);
        verify(talkDao).delete(id);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existing talk")
    void delete_WhenNotExists() {
        Long id = 1L;
        when(talkDao.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> talkService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Talk not found with ID: " + id);

        verify(talkDao).findById(id);
        verify(talkDao, never()).delete(anyLong());
    }
}