package com.aha.domain.workbook.service;

import com.aha.domain.exam.entity.Exam;
import com.aha.domain.exam.enums.ExamStatus;
import com.aha.domain.exam.repository.ExamRepository;
import com.aha.domain.workbook.dto.response.ExamWorkbookTypeResponseDto;
import com.aha.domain.workbook.entity.ExamWorkbookType;
import com.aha.domain.workbook.entity.WorkbookType;
import com.aha.domain.workbook.repository.ExamWorkbookTypeRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamWorkbookTypeService {

  private final ExamWorkbookTypeRepository examWorkbookTypeRepository;
  private final ExamRepository examRepository;

  public List<ExamWorkbookTypeResponseDto> getExamWorkbookType(String examCode) {

    Exam exam = examRepository.findByCode(examCode)
        .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));
    if (exam.getStatus()!= ExamStatus.ACTIVE) {
      throw new BusinessException(ErrorCode.EXAM_INACTIVE);
    }

    List<ExamWorkbookType> examWorkbookTypes = examWorkbookTypeRepository.findByExamIdAndIsActiveTrueWithWorkbookType(
        exam.getId());
    return  examWorkbookTypes.stream()
        .map(this::convertToResponseDto).toList();

  }

  private ExamWorkbookTypeResponseDto convertToResponseDto(ExamWorkbookType ewt) {
    WorkbookType wt = ewt.getWorkbookType();
    return new ExamWorkbookTypeResponseDto(
        wt.getCode(),
        wt.getName(),
        wt.getDisplayOrder()
    );
  }

}
