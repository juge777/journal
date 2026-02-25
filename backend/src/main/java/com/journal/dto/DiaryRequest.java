package com.journal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DiaryRequest {

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Content is required")
    private String content;

    @Size(max = 50, message = "Mood must not exceed 50 characters")
    private String mood;

    @Size(max = 50, message = "Weather must not exceed 50 characters")
    private String weather;
}
