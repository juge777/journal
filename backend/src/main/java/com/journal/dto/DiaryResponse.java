package com.journal.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiaryResponse {

    private Long id;
    private String title;
    private String content;
    private String mood;
    private String weather;
    private LocalDate diaryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
