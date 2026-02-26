package com.journal.repository;

import com.journal.entity.Diary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    Page<Diary> findByUserIdOrderByDiaryDateDescCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT d FROM Diary d WHERE d.userId = :userId AND (d.title LIKE %:keyword% OR d.content LIKE %:keyword%) ORDER BY d.diaryDate DESC, d.createdAt DESC")
    Page<Diary> searchByKeywordAndUserId(@Param("keyword") String keyword, @Param("userId") Long userId, Pageable pageable);

    List<Diary> findByUserIdOrderByDiaryDateDescCreatedAtDesc(Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);
}
