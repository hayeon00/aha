package com.aha.domain.learningnote.repository;

import com.aha.domain.learningnote.entity.LearningNoteContent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface LearningNoteContentRepository extends JpaRepository<LearningNoteContent, Long> {

}
