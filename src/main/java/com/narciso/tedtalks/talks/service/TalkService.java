package com.narciso.tedtalks.talks.service;

import com.narciso.tedtalks.common.exception.ResourceNotFoundException;
import com.narciso.tedtalks.imports.dto.CsvRecord;
import com.narciso.tedtalks.speakers.domain.Speaker;
import com.narciso.tedtalks.speakers.service.SpeakerService;
import com.narciso.tedtalks.talks.dao.TalkDao;
import com.narciso.tedtalks.talks.domain.Talk;
import com.narciso.tedtalks.talks.dto.CreateTalkDto;
import com.narciso.tedtalks.talks.dto.TalkDto;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TalkService {
    private final TalkDao talkDao;
    private final SpeakerService speakerService;
    private final DateTimeFormatter inputDateFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter CREATE_DTO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Transactional
    public void saveIfNotExists(CsvRecord rec, Long speakerId, int views, int likes) {
        YearMonth ym = YearMonth.parse(rec.getDate(), inputDateFormatter);
        boolean exists = talkDao.findByTitleAndSpeakerAndDate(
                rec.getTitle(), speakerId, ym
        ).isPresent();
        if (!exists) {
            Assert.notNull(speakerId, "Speaker ID cannot be null for saving talk");
            Assert.hasText(rec.getTitle(), "Talk title cannot be empty");

            Talk t = Talk.builder()
                    .title(rec.getTitle())
                    .date(ym)
                    .views(views)
                    .likes(likes)
                    .link(rec.getLink())
                    .speakerId(speakerId)
                    .build();
            talkDao.create(t);
        }
    }

    @Transactional
    public TalkDto create(CreateTalkDto dto) {
        Assert.notNull(dto, "CreateTalkDto cannot be null");

        Long finalSpeakerId;

        if (dto.getSpeakerId() != null) {
            try {
                Speaker existingSpeaker = speakerService.findById(dto.getSpeakerId());
                finalSpeakerId = existingSpeaker.getId();
            } catch (ResourceNotFoundException e) {
                throw new IllegalArgumentException("Speaker not found with provided speakerId: " + dto.getSpeakerId(), e);
            }
        } else if (StringUtils.isNotBlank(dto.getSpeakerName())) {
            Speaker speaker = speakerService.findOrCreate(dto.getSpeakerName());
            Assert.notNull(speaker, "Failed to find or create speaker");
            Assert.notNull(speaker.getId(), "Speaker ID is null after findOrCreate");
            finalSpeakerId = speaker.getId();
        } else {
            throw new IllegalArgumentException("Either speakerId or a non-blank speakerName must be provided to create a talk.");
        }

        YearMonth talkDate;
        try {
            talkDate = YearMonth.parse(dto.getDate(), CREATE_DTO_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format for date: '" + dto.getDate() + "'. Expected format: yyyy-MM.", e);
        }

        Talk talkToCreate = Talk.builder()
                .title(dto.getTitle())
                .date(talkDate)
                .views(dto.getViews())
                .likes(dto.getLikes())
                .link(dto.getLink())
                .speakerId(finalSpeakerId)
                .build();

        Talk createdTalkDomain = talkDao.create(talkToCreate);

        return findById(createdTalkDomain.getId());
    }

    @Transactional
    public TalkDto update(Long id, Talk talk) {
        Assert.notNull(id, "Talk ID for update cannot be null");
        Assert.notNull(talk, "Talk object cannot be null");
        Assert.hasText(talk.getTitle(), "Talk title cannot be empty");
        Assert.notNull(talk.getDate(), "Talk date cannot be null");
        Assert.notNull(talk.getSpeakerId(), "Speaker ID cannot be null");

        Talk existingTalkDomain = talkDao.findById(id)
                .map(dto -> Talk.builder()
                        .id(dto.getId())
                        .title(dto.getTitle())
                        .date(dto.getDate())
                        .views(dto.getViews())
                        .likes(dto.getLikes())
                        .link(dto.getLink())
                        .speakerId(dto.getSpeakerId())
                        .build())
                .orElseThrow(() -> new ResourceNotFoundException("Talk not found with ID: " + id));



        speakerService.findById(talk.getSpeakerId());

        existingTalkDomain.setTitle(talk.getTitle());
        existingTalkDomain.setDate(talk.getDate());
        existingTalkDomain.setViews(talk.getViews());
        existingTalkDomain.setLikes(talk.getLikes());
        existingTalkDomain.setLink(talk.getLink());
        existingTalkDomain.setSpeakerId(talk.getSpeakerId());

        talkDao.update(existingTalkDomain);

        return findById(id);
    }

    @Transactional
    public void delete(Long id) {
        Assert.notNull(id, "Talk ID for delete cannot be null");
        findById(id);

        talkDao.delete(id);
    }


    public Page<TalkDto> findAll(Pageable pageable) {
        return talkDao.findAll(pageable);
    }

    public TalkDto findById(Long id) {
        Assert.notNull(id, "Talk ID cannot be null");
        return talkDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Talk not found with ID: " + id));
    }
}