package com.journal.service;

import com.journal.dto.DiaryRequest;
import com.journal.dto.DiaryResponse;
import com.journal.entity.Diary;
import com.journal.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DiaryService {

    private final DiaryRepository diaryRepository;

    public DiaryResponse create(DiaryRequest request) {
        Diary diary = new Diary();
        diary.setTitle(request.getTitle());
        diary.setContent(request.getContent());
        diary.setMood(request.getMood());
        diary.setWeather(request.getWeather());
        diary.setDiaryDate(request.getDiaryDate() != null ? request.getDiaryDate() : LocalDate.now());
        diary = diaryRepository.save(diary);
        return toResponse(diary);
    }

    public DiaryResponse update(Long id, DiaryRequest request) {
        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diary not found with id: " + id));
        diary.setTitle(request.getTitle());
        diary.setContent(request.getContent());
        diary.setMood(request.getMood());
        diary.setWeather(request.getWeather());
        if (request.getDiaryDate() != null) {
            diary.setDiaryDate(request.getDiaryDate());
        }
        diary = diaryRepository.save(diary);
        return toResponse(diary);
    }

    public void delete(Long id) {
        if (!diaryRepository.existsById(id)) {
            throw new RuntimeException("Diary not found with id: " + id);
        }
        diaryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public DiaryResponse getById(Long id) {
        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diary not found with id: " + id));
        return toResponse(diary);
    }

    @Transactional(readOnly = true)
    public Page<DiaryResponse> getList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "diaryDate", "createdAt"));
        return diaryRepository.findAllByOrderByDiaryDateDescCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<DiaryResponse> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "diaryDate", "createdAt"));
        return diaryRepository.searchByKeyword(keyword, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<DiaryResponse> exportAll() {
        return diaryRepository.findAllByOrderByDiaryDateDescCreatedAtDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private DiaryResponse toResponse(Diary diary) {
        return new DiaryResponse(
                diary.getId(),
                diary.getTitle(),
                diary.getContent(),
                diary.getMood(),
                diary.getWeather(),
                diary.getDiaryDate(),
                diary.getCreatedAt(),
                diary.getUpdatedAt()
        );
    }
}
