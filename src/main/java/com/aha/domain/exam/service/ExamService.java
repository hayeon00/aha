package com.aha.domain.exam.service;

import com.aha.domain.exam.dto.response.ExamResponseDto;
import com.aha.domain.exam.repository.ExamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamService {

    private final ExamRepository examRepository;

    public List<ExamResponseDto> getActiveExams() {
        return examRepository.findByIsActiveTrue()
                .stream()
                .map(ExamResponseDto::new)
                .toList();
    }
}