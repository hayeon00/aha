package com.aha.domain.exam.service;

import com.aha.domain.exam.dto.response.ExamResponseDto;
import com.aha.domain.exam.entity.Exam;
import com.aha.domain.exam.entity.ExamVersion;
import com.aha.domain.exam.entity.ExamVersionStatus;
import com.aha.domain.exam.enums.ExamStatus;
import com.aha.domain.exam.repository.ExamRepository;
import com.aha.domain.exam.repository.ExamVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamService {


    private final ExamRepository examRepository;
    private final ExamVersionRepository examVersionRepository;

    public List<ExamResponseDto> getSupportedExams() {
        List<Exam> exams = examRepository.findAllByStatusOrderByIdAsc(ExamStatus.ACTIVE);

        return exams.stream()
                .map(exam -> {
                    ExamVersion activeVersion = examVersionRepository
                            .findFirstByExam_IdAndStatusOrderByVersionNoDesc(
                                    exam.getId(),
                                    ExamVersionStatus.ACTIVE
                            )
                            .orElse(null);

                    return ExamResponseDto.from(exam, activeVersion);
                })
                .toList();
    }
}