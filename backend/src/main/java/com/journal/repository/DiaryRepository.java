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

    Page<Diary> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT d FROM Diary d WHERE d.title LIKE %:keyword% OR d.content LIKE %:keyword% ORDER BY d.createdAt DESC")
    Page<Diary> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    List<Diary> findAllByOrderByCreatedAtDesc();
}
