package com.kb_card.common.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "헬스체크", description = "서버 상태 확인")
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "Application Health Good!";
    }

}
