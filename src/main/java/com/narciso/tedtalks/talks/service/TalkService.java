package com.narciso.tedtalks.talks.service;

import com.narciso.tedtalks.common.exception.ResourceNotFoundException;
import com.narciso.tedtalks.imports.dto.CsvRecord;
import com.narciso.tedtalks.speakers.service.SpeakerService;
import com.narciso.tedtalks.talks.dao.TalkDao;
import com.narciso.tedtalks.talks.domain.Talk;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TalkService {
    private final TalkDao talkDao;
    private final SpeakerService speakerService;
    private final DateTimeFormatter inputDateFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

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
    public Talk create(Talk talk) {
        Assert.notNull(talk, "Talk object cannot be null");
        Assert.isNull(talk.getId(), "Talk ID must be null for creation");
        Assert.hasText(talk.getTitle(), "Talk title cannot be empty");
        Assert.notNull(talk.getDate(), "Talk date cannot be null");
        Assert.notNull(talk.getSpeakerId(), "Speaker ID cannot be null");

        speakerService.findById(talk.getSpeakerId());

        return talkDao.create(talk);
    }

    @Transactional
    public Talk update(Long id, Talk talk) {
        Assert.notNull(id, "Talk ID for update cannot be null");
        Assert.notNull(talk, "Talk object cannot be null");
        Assert.hasText(talk.getTitle(), "Talk title cannot be empty");
        Assert.notNull(talk.getDate(), "Talk date cannot be null");
        Assert.notNull(talk.getSpeakerId(), "Speaker ID cannot be null");

        Talk existingTalk = findById(id);

        speakerService.findById(talk.getSpeakerId());

        existingTalk.setTitle(talk.getTitle());
        existingTalk.setDate(talk.getDate());
        existingTalk.setViews(talk.getViews());
        existingTalk.setLikes(talk.getLikes());
        existingTalk.setLink(talk.getLink());
        existingTalk.setSpeakerId(talk.getSpeakerId());

        talkDao.update(existingTalk);
        return existingTalk;
    }

    @Transactional
    public void delete(Long id) {
        Assert.notNull(id, "Talk ID for delete cannot be null");
        findById(id);

        talkDao.delete(id);
    }


    public Page<Talk> findAll(Pageable pageable) {
        return talkDao.findAll(pageable);
    }

    public Talk findById(Long id) {
        Assert.notNull(id, "Talk ID cannot be null");
        return talkDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Talk not found with ID: " + id));
    }
}