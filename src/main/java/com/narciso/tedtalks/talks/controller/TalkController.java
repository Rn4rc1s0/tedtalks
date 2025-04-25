package com.narciso.tedtalks.talks.controller;

import com.narciso.tedtalks.common.utils.SortUtils;
import com.narciso.tedtalks.talks.domain.Talk;
import com.narciso.tedtalks.talks.service.TalkService;
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
    public Page<Talk> getAllTalks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title,asc") String[] sort
    ) {
        Sort sorting = SortUtils.createSort(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);
        return talkService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Talk> getTalkById(@PathVariable Long id) {
        Talk talk = talkService.findById(id);
        return ResponseEntity.ok(talk);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Talk createTalk(@RequestBody Talk talk) {
        return talkService.create(talk);
    }

    @PutMapping("/{id}")
    public Talk updateTalk(@PathVariable Long id, @RequestBody Talk talk) {
        return talkService.update(id, talk);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTalk(@PathVariable Long id) {
        talkService.delete(id);
    }
}