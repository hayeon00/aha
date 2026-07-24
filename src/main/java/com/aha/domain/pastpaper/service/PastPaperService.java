package com.aha.domain.pastpaper.service;

import com.aha.domain.exam.entity.Exam;
import com.aha.domain.exam.entity.ExamVersion;
import com.aha.domain.exam.repository.ExamVersionRepository;
import com.aha.domain.pastpaper.dto.response.PastPaperResponseDto;
import com.aha.domain.pastpaper.enums.PastPaperStatus;
import com.aha.domain.pastpaper.repository.PastPaperRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PastPaperService {

    private final PastPaperRepository pastPaperRepository;
    private final ExamVersionRepository examVersionRepository;

    public List<PastPaperResponseDto> getPastPapers(Long versionId){

        ExamVersion version = examVersionRepository.findByIdWithExam(versionId)
            .orElseThrow(()->new BusinessException(ErrorCode.EXAM_VERSION_NOT_FOUND));
        Exam exam = version.getExam();
        exam.validateActive();
        version.validateActive();

        return pastPaperRepository.findByExamVersion_IdAndStatus(versionId, PastPaperStatus.PUBLISHED)
            .stream().map(pp->PastPaperResponseDto.of(pp,exam)).toList();
    }

}
