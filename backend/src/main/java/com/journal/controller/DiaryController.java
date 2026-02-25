package com.journal.controller;

import com.journal.dto.DiaryRequest;
import com.journal.dto.DiaryResponse;
import com.journal.service.DiaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/diaries")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping
    public ResponseEntity<DiaryResponse> create(@Valid @RequestBody DiaryRequest request) {
        DiaryResponse response = diaryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiaryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DiaryRequest request) {
        DiaryResponse response = diaryService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        diaryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiaryResponse> getById(@PathVariable Long id) {
        DiaryResponse response = diaryService.getById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<DiaryResponse>> getList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<DiaryResponse> response = diaryService.getList(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<DiaryResponse>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<DiaryResponse> response = diaryService.search(keyword, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportAll() {
        List<DiaryResponse> diaries = diaryService.exportAll();

        StringBuilder json = new StringBuilder("[\n");
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        for (int i = 0; i < diaries.size(); i++) {
            DiaryResponse d = diaries.get(i);
            json.append("  {\n");
            json.append("    \"id\": ").append(d.getId()).append(",\n");
            json.append("    \"title\": ").append(escapeJson(d.getTitle())).append(",\n");
            json.append("    \"content\": ").append(escapeJson(d.getContent())).append(",\n");
            json.append("    \"mood\": ").append(escapeJson(d.getMood())).append(",\n");
            json.append("    \"weather\": ").append(escapeJson(d.getWeather())).append(",\n");
            json.append("    \"createdAt\": \"").append(d.getCreatedAt().format(formatter)).append("\",\n");
            json.append("    \"updatedAt\": \"").append(d.getUpdatedAt().format(formatter)).append("\"\n");
            json.append("  }");
            if (i < diaries.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }
        json.append("]");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"diaries-export.json\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json.toString());
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            org.springframework.dao.DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Data validation failed"));
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(message));
    }

    record ErrorResponse(String message) {}
}
