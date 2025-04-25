package com.narciso.tedtalks.speakers.controller;

import com.narciso.tedtalks.common.utils.SortUtils;
import com.narciso.tedtalks.speakers.domain.Speaker;
import com.narciso.tedtalks.speakers.service.SpeakerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/speakers")
@RequiredArgsConstructor
public class SpeakerController {
    private final SpeakerService speakerService;

    @GetMapping
    public Page<Speaker> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name,asc") String[] sort) {
        Sort sorting = SortUtils.createSort(sort);

        Pageable pageable = PageRequest.of(page, size, sorting);

        return speakerService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public Speaker getOne(@PathVariable Long id) {
        return speakerService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Speaker create(@RequestBody Speaker speaker) {
        return speakerService.create(speaker);
    }

    @PutMapping("/{id}")
    public Speaker update(@PathVariable Long id, @RequestBody Speaker speaker) {
        return speakerService.update(id, speaker);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        speakerService.delete(id);
    }
}
