package com.narciso.tedtalks.speakers.controller;

import com.narciso.tedtalks.speakers.domain.MostInfluentialSpeaker;
import com.narciso.tedtalks.speakers.dto.SpeakerInfluenceDto;
import com.narciso.tedtalks.speakers.service.InfluenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/speakers/influence")
@RequiredArgsConstructor
public class InfluenceController {
    private final InfluenceService influenceService;

    @GetMapping("/most-influential")
    public ResponseEntity<MostInfluentialSpeaker> getMostInfluential(
            @RequestParam("year") int year) {
        Optional<MostInfluentialSpeaker> result =
                influenceService.findMostInfluentialSpeaker(year);
        return result
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping
    public ResponseEntity<List<SpeakerInfluenceDto>> analyze(
            @RequestParam(name = "year", required = false) Integer year
    ) {
        List<SpeakerInfluenceDto> result = influenceService.analyzeInfluence(Optional.ofNullable(year));
        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }
}
