package com.aha.domain.workbook.service;

import com.aha.domain.workbook.dto.response.WorkbookTypeResponseDto;
import com.aha.domain.workbook.repository.WorkbookTypeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WorkbookTypeService {
    private final WorkbookTypeRepository workbookTypeRepository;

    @Transactional(readOnly = true)
    public List<WorkbookTypeResponseDto> getWorkbookTypes(){

        return workbookTypeRepository.findAllByOrderByDisplayOrderAsc().stream().map(WorkbookTypeResponseDto::from).toList();

    }
}
