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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }
        throw new RuntimeException("未登录或登录已过期");
    }

    public DiaryResponse create(DiaryRequest request) {
        Long userId = getCurrentUserId();
        Diary diary = new Diary();
        diary.setUserId(userId);
        diary.setTitle(request.getTitle());
        diary.setContent(request.getContent());
        diary.setMood(request.getMood());
        diary.setWeather(request.getWeather());
        diary.setDiaryDate(request.getDiaryDate() != null ? request.getDiaryDate() : LocalDate.now());
        diary = diaryRepository.save(diary);
        return toResponse(diary);
    }

    public DiaryResponse update(Long id, DiaryRequest request) {
        Long userId = getCurrentUserId();
        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("日记不存在"));

        if (!diary.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此日记");
        }

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
        Long userId = getCurrentUserId();
        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("日记不存在"));

        if (!diary.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此日记");
        }

        diaryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public DiaryResponse getById(Long id) {
        Long userId = getCurrentUserId();
        Diary diary = diaryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("日记不存在"));

        if (!diary.getUserId().equals(userId)) {
            throw new RuntimeException("无权查看此日记");
        }

        return toResponse(diary);
    }

    @Transactional(readOnly = true)
    public Page<DiaryResponse> getList(int page, int size) {
        Long userId = getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "diaryDate", "createdAt"));
        return diaryRepository.findByUserIdOrderByDiaryDateDescCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<DiaryResponse> search(String keyword, int page, int size) {
        Long userId = getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "diaryDate", "createdAt"));
        return diaryRepository.searchByKeywordAndUserId(keyword, userId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<DiaryResponse> exportAll() {
        Long userId = getCurrentUserId();
        return diaryRepository.findByUserIdOrderByDiaryDateDescCreatedAtDesc(userId).stream()
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
