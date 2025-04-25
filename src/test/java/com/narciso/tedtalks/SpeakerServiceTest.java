package com.narciso.tedtalks.speakers.service;

import com.narciso.tedtalks.common.exception.ResourceNotFoundException;
import com.narciso.tedtalks.speakers.dao.SpeakerDao;
import com.narciso.tedtalks.speakers.domain.Speaker;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpeakerServiceTest {

    @Mock
    private SpeakerDao speakerDao;

    @InjectMocks
    private SpeakerService speakerService;

    @Test
    @DisplayName("Should find existing speaker by name")
    void findOrCreate_WhenSpeakerExists() {
        String name = "John Doe";
        Speaker speaker = Speaker.builder().id(1L).name(name).build();
        when(speakerDao.findByName(name)).thenReturn(Optional.of(speaker));

        Speaker result = speakerService.findOrCreate(name);

        assertThat(result).isEqualTo(speaker);
        verify(speakerDao).findByName(name);
        verify(speakerDao, never()).save(any());
    }

    @Test
    @DisplayName("Should create new speaker when not found")
    void findOrCreate_WhenSpeakerNotExists() {
        String name = "John Doe";
        Speaker newSpeaker = Speaker.builder().name(name).build();
        Speaker savedSpeaker = Speaker.builder().id(1L).name(name).build();

        when(speakerDao.findByName(name)).thenReturn(Optional.empty());
        when(speakerDao.save(newSpeaker)).thenReturn(savedSpeaker);

        Speaker result = speakerService.findOrCreate(name);

        assertThat(result).isEqualTo(savedSpeaker);
        verify(speakerDao).findByName(name);
        verify(speakerDao).save(newSpeaker);
    }

    @Test
    @DisplayName("Should return all speakers paginated")
    void findAll() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Speaker> speakers = List.of(
            Speaker.builder().id(1L).name("John").build(),
            Speaker.builder().id(2L).name("Jane").build()
        );
        Page<Speaker> expectedPage = new PageImpl<>(speakers, pageable, speakers.size());

        when(speakerDao.findAll(pageable)).thenReturn(expectedPage);

        Page<Speaker> result = speakerService.findAll(pageable);

        assertThat(result).isEqualTo(expectedPage);
        verify(speakerDao).findAll(pageable);
    }

    @Test
    @DisplayName("Should find speaker by ID when exists")
    void findById_WhenExists() {
        long id = 1L;
        Speaker speaker = Speaker.builder().id(id).name("John").build();
        when(speakerDao.findById(id)).thenReturn(Optional.of(speaker));

        Speaker result = speakerService.findById(id);

        assertThat(result).isEqualTo(speaker);
        verify(speakerDao).findById(id);
    }

    @Test
    @DisplayName("Should throw exception when speaker not found by ID")
    void findById_WhenNotExists() {
        long id = 1L;
        when(speakerDao.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> speakerService.findById(id))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Speaker not found: " + id);

        verify(speakerDao).findById(id);
    }

    @Test
    @DisplayName("Should find speaker by name when exists")
    void findByName_WhenExists() {
        String name = "John";
        Speaker speaker = Speaker.builder().id(1L).name(name).build();
        when(speakerDao.findByName(name)).thenReturn(Optional.of(speaker));

        Speaker result = speakerService.findByName(name);

        assertThat(result).isEqualTo(speaker);
        verify(speakerDao).findByName(name);
    }

    @Test
    @DisplayName("Should throw exception when speaker not found by name")
    void findByName_WhenNotExists() {
        String name = "John";
        when(speakerDao.findByName(name)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> speakerService.findByName(name))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Speaker not found: " + name);

        verify(speakerDao).findByName(name);
    }

    @Test
    @DisplayName("Should create new speaker")
    void create() {
        Speaker newSpeaker = Speaker.builder().name("John").build();
        Speaker savedSpeaker = Speaker.builder().id(1L).name("John").build();
        when(speakerDao.save(newSpeaker)).thenReturn(savedSpeaker);

        Speaker result = speakerService.create(newSpeaker);

        assertThat(result).isEqualTo(savedSpeaker);
        verify(speakerDao).save(newSpeaker);
    }

    @Test
    @DisplayName("Should update existing speaker")
    void update_WhenExists() {
        long id = 1L;
        Speaker existingSpeaker = Speaker.builder().id(id).name("John").build();
        Speaker updatedSpeaker = Speaker.builder().id(id).name("John Updated").build();

        when(speakerDao.findById(id)).thenReturn(Optional.of(existingSpeaker));

        Speaker result = speakerService.update(id, updatedSpeaker);

        assertThat(result.getName()).isEqualTo("John Updated");
        verify(speakerDao).findById(id);
        verify(speakerDao).update(existingSpeaker);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existing speaker")
    void update_WhenNotExists() {
        long id = 1L;
        Speaker speaker = Speaker.builder().id(id).name("John").build();
        when(speakerDao.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> speakerService.update(id, speaker))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Speaker not found: " + id);

        verify(speakerDao).findById(id);
        verify(speakerDao, never()).update(any());
    }

    @Test
    @DisplayName("Should delete existing speaker")
    void delete_WhenExists() {
        long id = 1L;
        Speaker speaker = Speaker.builder().id(id).name("John").build();
        when(speakerDao.findById(id)).thenReturn(Optional.of(speaker));

        speakerService.delete(id);

        verify(speakerDao).findById(id);
        verify(speakerDao).delete(id);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existing speaker")
    void delete_WhenNotExists() {
        long id = 1L;
        when(speakerDao.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> speakerService.delete(id))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Speaker not found: " + id);

        verify(speakerDao).findById(id);
        verify(speakerDao, never()).delete(anyLong());
    }
}