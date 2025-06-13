package com.narciso.tedtalks.speakers.service;

import com.narciso.tedtalks.common.exception.ResourceNotFoundException;
import com.narciso.tedtalks.speakers.dao.SpeakerDao;
import com.narciso.tedtalks.speakers.domain.Speaker;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SpeakerService {
    private final SpeakerDao speakerDao;

    @Transactional
    public Speaker findOrCreate(String name) {
        return speakerDao.findByName(name)
                .orElseGet(() -> speakerDao.save(
                        Speaker.builder().name(name).build()
                ));
    }

    public Page<Speaker> findAll(Pageable pageable) {
        return speakerDao.findAll(pageable);
    }

    public Speaker findById(Long id) {
        return speakerDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Speaker not found: " + id));
    }

    public Speaker findByName(String name) {
        return speakerDao.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Speaker not found: " + name));
    }
    @Transactional
    public Speaker create(Speaker s) { return speakerDao.save(s); }

    @Transactional
    public Speaker update(Long id, Speaker s) {
        Speaker existing = findById(id);
        existing.setName(s.getName());
        speakerDao.update(existing);
        return existing;
    }
    @Transactional
    public void delete(Long id) {
        findById(id);
        speakerDao.delete(id);
    }
}
