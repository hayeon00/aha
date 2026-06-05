package com.aha.domain.workbook.service;

import com.aha.domain.problem.entity.ProblemChoice;
import com.aha.domain.workbook.dto.response.WorkbookAttemptResponseDto;
import com.aha.domain.workbook.dto.response.WorkbookAttemptResponseDto.ProblemChoiceResponse;
import com.aha.domain.workbook.dto.response.WorkbookAttemptResponseDto.WorkbookAttemptMeta;
import com.aha.domain.workbook.dto.response.WorkbookAttemptResponseDto.WorkbookItemResponse;
import com.aha.domain.workbook.entity.AttemptStatus;
import com.aha.domain.workbook.entity.Workbook;
import com.aha.domain.workbook.entity.WorkbookAttempt;
import com.aha.domain.workbook.entity.WorkbookItem;
import com.aha.domain.workbook.entity.WorkbookStatus;
import com.aha.domain.workbook.repository.WorkbookAttemptRepository;
import com.aha.domain.workbook.repository.WorkbookItemRepository;
import com.aha.domain.workbook.repository.WorkbookRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import com.aha.global.security.CustomUserDetails;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WorkbookAttemptService {

  private final WorkbookAttemptRepository workbookAttemptRepository;
  private final WorkbookRepository workbookRepository;
  private final WorkbookItemRepository workbookItemRepository;

  public WorkbookAttemptResponseDto startWorkbook(Long workbookId, CustomUserDetails userDetails) {
    
    Workbook workbook = workbookRepository.findById(workbookId)
        .orElseThrow(()->new BusinessException(ErrorCode.WORKBOOK_NOT_FOUND));

    if(workbook.getStatus()== WorkbookStatus.ARCHIVED){
      throw new BusinessException(ErrorCode.WORKBOOK_ARCHIVED);
    }

    Long userId =userDetails.getId();
    workbookAttemptRepository.findByWorkbook_IdAndUserId(workbookId,userId)
        .ifPresent(this::checkAttemptStatus);

    WorkbookAttempt attempt = WorkbookAttempt.startAttempt(userId,workbook,workbook.getTimeLimit());
    workbookAttemptRepository.save(
        attempt
    );

    WorkbookAttemptMeta meta = new WorkbookAttemptMeta(
        attempt.getId(),
        workbook.getTotalQuestionCount(),
        0,
        0,
        0
    );

    List<WorkbookItem> workbookItems = workbookItemRepository.findItemsWithProblemAndChoicesByWorkbookId(workbookId);
    List<WorkbookItemResponse> workbookItemResponses = workbookItems.stream().map(this::convertToWorkbookItemResponse).toList();

    return new WorkbookAttemptResponseDto(meta,workbookItemResponses);

  }

  private void checkAttemptStatus(WorkbookAttempt workbookAttempt) {
    if(workbookAttempt.getStatus()== AttemptStatus.IN_PROGRESS)
      throw new BusinessException(ErrorCode.WORKBOOK_ATTEMPT_ALREADY_EXIST,workbookAttempt.getId());
  }

  private WorkbookItemResponse convertToWorkbookItemResponse(WorkbookItem workbookItem) {

    List<ProblemChoiceResponse> ChoiceResponses = workbookItem.getProblem().getChoices()
        .stream().map(this::converToProblemChoiceResponse).toList();

    return new WorkbookItemResponse(
        workbookItem.getItemNo(),
        workbookItem.getId(),
        workbookItem.getProblem().getChoiceType(),
        workbookItem.getProblem().getAnswerType(),
        workbookItem.getProblem().getQuestionContentJson(),
        ChoiceResponses
    );

  }

  private ProblemChoiceResponse converToProblemChoiceResponse(ProblemChoice problemChoice) {
    return new ProblemChoiceResponse(
        problemChoice.getChoiceNo(),
        problemChoice.getId(),
        problemChoice.getChoiceContentJson()
    );
  }


}
