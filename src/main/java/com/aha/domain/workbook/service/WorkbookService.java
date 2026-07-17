package com.aha.domain.workbook.service;

import com.aha.domain.exam.entity.ExamPart;
import com.aha.domain.exam.entity.ExamScopeNode;
import com.aha.domain.workbook.dto.response.AttemptStartResponseDto;
import com.aha.domain.workbook.dto.response.WorkbookItemResponseDto;
import com.aha.domain.workbook.dto.response.WorkbookResponseDto;
import com.aha.domain.exam.entity.ExamVersionStatus;
import com.aha.domain.exam.repository.ExamVersionRepository;
import com.aha.domain.workbook.enums.AttemptStatus;
import com.aha.domain.workbook.entity.Problem;
import com.aha.domain.workbook.entity.ProblemChoice;
import com.aha.domain.workbook.entity.Workbook;
import com.aha.domain.workbook.entity.WorkbookAttempt;
import com.aha.domain.workbook.enums.WorkbookStatus;
import com.aha.domain.workbook.enums.WorkbookTypeCode;
import com.aha.domain.workbook.repository.PastExamWorkbookRepository;
import com.aha.domain.workbook.repository.WorkbookAttemptRepository;
import com.aha.domain.workbook.repository.WorkbookItemRepository;
import com.aha.domain.workbook.repository.WorkbookRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import com.aha.global.security.CustomUserDetails;
import jakarta.validation.constraints.Min;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkbookService {

    private final WorkbookRepository workbookRepository;
    private final PastExamWorkbookRepository pastExamWorkbookRepository;
    private final ExamVersionRepository examVersionRepository;
    private final WorkbookAttemptRepository workbookAttemptRepository;
    private final WorkbookItemRepository workbookItemRepository;

    @Transactional(readOnly = true)
    public List<WorkbookResponseDto> getWorkbooks(Long examVersionId,
        WorkbookTypeCode workbookTypeCode) {

        boolean isExisted = examVersionRepository.existsByIdAndStatus(examVersionId,
            ExamVersionStatus.ACTIVE);
        if (!isExisted) {
            throw new BusinessException(ErrorCode.EXAM_VERSION_NOT_FOUND);
        }

        List<WorkbookResponseDto> response = null;

        if (workbookTypeCode == WorkbookTypeCode.PAST) {
            response = pastExamWorkbookRepository.findPastExamWorkbookByExamVersion_Id(
                    examVersionId, WorkbookStatus.PUBLISHED)
                .stream().map(WorkbookResponseDto::from).toList();
        } else {
            response = new ArrayList<>();
        }

        return response;
    }

    @Transactional
    public AttemptStartResponseDto startOrResumeAttempt(Long workbookId,
        CustomUserDetails userDetails) {
        Workbook workbook = workbookRepository.findByIdWithExamVersionAndPastExamWorkBook(
                workbookId)
            .orElseThrow(() -> new BusinessException(ErrorCode.WORKBOOK_NOT_FOUND));

        workbook.validateStartAttempt();

        Optional<WorkbookAttempt> existedAttempt = workbookAttemptRepository.findByWorkbookIdAndUserIdStatus(workbookId, userDetails.getId(),
                AttemptStatus.SOLVING);
        if(existedAttempt.isPresent()){
            return AttemptStartResponseDto.of(existedAttempt.get(), workbook);
        }

        WorkbookAttempt workbookAttempt = workbookAttemptRepository.save(
            WorkbookAttempt.create(userDetails.getId(), workbook));
        return AttemptStartResponseDto.of(workbookAttempt, workbook);
    }

    @Transactional(readOnly = true)
    public AttemptStartResponseDto getExistingAttempt(Long workbookId, CustomUserDetails userDetails) {
        Workbook workbook = workbookRepository.findByIdWithExamVersionAndPastExamWorkBook(
                workbookId)
            .orElseThrow(() -> new BusinessException(ErrorCode.WORKBOOK_NOT_FOUND));

        workbook.validateStartAttempt();

        WorkbookAttempt existingAttempt = workbookAttemptRepository.findByWorkbookIdAndUserIdStatus(workbookId, userDetails.getId(),
            AttemptStatus.SOLVING).orElseThrow(()->new BusinessException(ErrorCode.ATTEMPT_NOT_FOUND));

        return AttemptStartResponseDto.of(existingAttempt, workbook);
    }

    @Transactional(readOnly = true)
    public List<WorkbookItemResponseDto> getWorkbookItems(Long workbookId,
        CustomUserDetails userDetails) {

        Long userId = userDetails.getId();
        WorkbookAttempt workbookAttempt = workbookAttemptRepository.findByWorkbookIdAndUserIdWithWorkbookAndExamVersionAndExam(
                workbookId, userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ATTEMPT_NOT_FOUND));

        Workbook workbook = workbookAttempt.getWorkbook();
        workbook.validateGetItems();

        return workbookItemRepository.findByWorkbook_Id(workbookId).stream()
            .map(wi -> {
                Problem problem = wi.getProblem();
                List<ProblemChoice> problemChoices = problem.getProblemChoices();
                ExamScopeNode examScopeNode = problem.getExamScopeNode();
                ExamPart examPart = examScopeNode.getExamPart();
                return WorkbookItemResponseDto.ofSolving(wi, problem, problemChoices, examPart);
            }).toList();

        //GRADED는 다음 이슈에서
    }

}
