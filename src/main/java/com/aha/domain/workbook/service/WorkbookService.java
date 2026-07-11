package com.aha.domain.workbook.service;

import com.aha.domain.workbook.dto.response.WorkbookResponseDto;
import com.aha.domain.exam.entity.ExamVersionStatus;
import com.aha.domain.exam.repository.ExamVersionRepository;
import com.aha.domain.workbook.entity.WorkbookStatus;
import com.aha.domain.workbook.entity.WorkbookTypeCode;
import com.aha.domain.workbook.repository.PastExamWorkbookRepository;
import com.aha.global.exception.BusinessException;
import com.aha.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkbookService {

    private final PastExamWorkbookRepository pastExamWorkbookRepository;
    private final ExamVersionRepository examVersionRepository;

    @Transactional(readOnly = true)
    public List<WorkbookResponseDto> getWorkbooks(Long examVersionId, WorkbookTypeCode workbookTypeCode){

        boolean isExisted = examVersionRepository.existsByIdAndStatus(examVersionId, ExamVersionStatus.ACTIVE);

        if(!isExisted){
            log.warn("[WorkbookService] 조회 실패 - 존재하지 않거나 비활성화된 ExamVersion ID: {}", examVersionId);
            throw new BusinessException(ErrorCode.EXAM_VERSION_NOT_FOUND);
        }

        List<WorkbookResponseDto> response = null;

        if(workbookTypeCode==WorkbookTypeCode.PAST){
            response = pastExamWorkbookRepository.findPastExamWorkbookByExamVersion_Id(examVersionId, WorkbookStatus.PUBLISHED)
                .stream().map(WorkbookResponseDto::from).toList();
        }else{
            response = new ArrayList<>();
        }

        return response;
    }
}
