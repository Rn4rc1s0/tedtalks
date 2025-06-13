package com.narciso.tedtalks.talks.controller;

import com.narciso.tedtalks.common.utils.SortUtils;
import com.narciso.tedtalks.talks.domain.Talk;
import com.narciso.tedtalks.talks.dto.CreateTalkDto;
import com.narciso.tedtalks.talks.dto.TalkDto;
import com.narciso.tedtalks.talks.service.TalkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/talks")
@RequiredArgsConstructor
public class TalkController {
    private final TalkService talkService;

    @GetMapping
    public Page<TalkDto> getAllTalks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title,asc") String[] sort
    ) {
        Sort sorting = SortUtils.createSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        return talkService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TalkDto> getTalkById(@PathVariable Long id) {
        TalkDto talkDto = talkService.findById(id);
        return ResponseEntity.ok(talkDto);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TalkDto createTalk(@Valid @RequestBody CreateTalkDto createTalkDto) {

        return talkService.create(createTalkDto);
    }

    @PutMapping("/{id}")
    public TalkDto updateTalk(@PathVariable Long id, @RequestBody Talk talk) {
        return talkService.update(id, talk);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTalk(@PathVariable Long id) {
        talkService.delete(id);
    }
}