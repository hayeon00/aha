package com.aha.domain.workbook.repository;

import com.aha.domain.workbook.entity.WorkbookType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface WorkbookTypeRepository extends JpaRepository<WorkbookType, Long> {


  Optional<WorkbookType> findByCode(String workbookTypeCode);
}
