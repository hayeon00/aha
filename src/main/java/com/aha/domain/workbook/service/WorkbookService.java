package com.aha.domain.workbook.service;

import com.aha.domain.exam.entity.Exam;
import com.aha.domain.exam.repository.ExamRepository;
import com.aha.domain.workbook.dto.response.WorkbookGroupResponseDto;
import com.aha.domain.workbook.dto.response.WorkbookGroupResponseDto.WorkbookData;
import com.aha.domain.workbook.dto.response.WorkbookGroupResponseDto.WorkbookGroup;
import com.aha.domain.workbook.dto.response.WorkbookGroupResponseDto.WorkbookMeta;
import com.aha.domain.workbook.entity.AttemptStatus;
import com.aha.domain.workbook.entity.Workbook;
import com.aha.domain.workbook.entity.WorkbookAttempt;
import com.aha.domain.workbook.entity.WorkbookStatus;
import com.aha.domain.workbook.entity.WorkbookType;
import com.aha.domain.workbook.repository.ExamWorkbookTypeRepository;
import com.aha.domain.workbook.repository.WorkbookAttemptRepository;
import com.aha.domain.workbook.repository.WorkbookRepository;
import com.aha.domain.workbook.repository.WorkbookTypeRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkbookService {

  private final WorkbookRepository workbookRepository;
  private final WorkbookAttemptRepository workbookAttemptRepository;
  private final ExamRepository examRepository;
  private final WorkbookTypeRepository workbookTypeRepository;
  private final ExamWorkbookTypeRepository examWorkbookTypeRepository;

  public WorkbookGroupResponseDto getWorkbookList(Long userId, String examCode,
      String workbookTypeCode) {

    Exam exam = examRepository.findByCode(examCode)
        .orElseThrow(() -> new BusinessException(ErrorCode.EXAM_NOT_FOUND));
    if (!exam.isActive()) {
      throw new BusinessException(ErrorCode.EXAM_INACTIVE);
    }

    WorkbookType workbookType = workbookTypeRepository.findByCode(workbookTypeCode)
        .orElseThrow(() -> new BusinessException(ErrorCode.WORKBOOK_TYPE_NOT_FOUND));

    examWorkbookTypeRepository.findByExam_IdAndWorkbookType_IdAndIsActiveTrue(exam.getId(),
            workbookType.getId())
        .orElseThrow(() -> new BusinessException(ErrorCode.WORKBOOK_TYPE_UNSUPPORTED_EXAM));

    WorkbookMeta workbookMeta = new WorkbookMeta(
        examCode,
        exam.getName(),
        workbookTypeCode,
        workbookType.getName()
    );

    log.info("examCode ={}, workbookTypeCode={}",examCode,workbookTypeCode);
    List<Workbook> workbooks = workbookRepository.findReviewedWorkbooks(examCode, workbookTypeCode,
        WorkbookStatus.REVIEWED);
    log.info("workbooks ={}",workbooks);
    if (workbooks.isEmpty()) {
      return new WorkbookGroupResponseDto(workbookMeta, List.of());
    }

    List<Long> workbookIds = workbooks.stream().map(Workbook::getId).toList();

    List<WorkbookAttempt> workbookAttempts = workbookAttemptRepository.findLatestAttempts(userId,
        workbookIds);

    Map<Long, WorkbookAttempt> latestAttemptByWorkbookId = workbookAttempts.stream()
        .collect(Collectors.toMap(wa -> wa.getWorkbook().getId(), wa -> wa));

    List<WorkbookData> workbookDataList = workbooks.stream()
        .map(w -> convertToWorkbookData(w, latestAttemptByWorkbookId.get(w.getId()))).toList();

    Map<Integer, List<WorkbookData>> groupedByExamYear = workbookDataList.stream()
        .collect(Collectors.groupingBy(WorkbookData::getExamYear, LinkedHashMap::new,
            Collectors.toList()));

    List<WorkbookGroup> groups = groupedByExamYear.entrySet()
        .stream()
        .map(entry -> new WorkbookGroup(entry.getKey(), entry.getValue()))
        .toList();

    return new WorkbookGroupResponseDto(workbookMeta, groups);
  }

  private WorkbookData convertToWorkbookData(Workbook w, WorkbookAttempt wa) {
    return new WorkbookData(
        w.getId(),
        w.getExamYear(),
        w.getNo(),
        w.getTotalQuestionCount(),
        w.getTimeLimit(),
        w.getStatus(),
        w.getStatus().getName(),
        wa == null ? AttemptStatus.BEFORE_START : wa.getStatus(),
        wa == null ? AttemptStatus.BEFORE_START.getName() : wa.getStatus().getName(),
        resolveIsPassed(wa)
    );
  }

  private Boolean resolveIsPassed(WorkbookAttempt wa) {
    if (wa == null || wa.getWorkbookResult() == null) {
      return null;
    }

    return wa.getWorkbookResult().isPassed();
  }
}
