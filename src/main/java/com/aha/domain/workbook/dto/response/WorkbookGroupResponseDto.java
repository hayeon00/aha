package com.aha.domain.workbook.dto.response;


import com.aha.domain.workbook.entity.AttemptStatus;
import com.aha.domain.workbook.entity.WorkbookStatus;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WorkbookGroupResponseDto {

    private WorkbookMeta meta;
    private List<WorkbookGroup> groups;

    @Getter
    @AllArgsConstructor
    public static class WorkbookMeta{
        private String examCode;
        private String examName;
        private String workbookTypeCode;
        private String workbookTypeName;
    }

    @Getter
    @AllArgsConstructor
    public static class WorkbookGroup{
        private Integer examYear;
        private List<WorkbookData>  workbooks;
    }

    @Getter
    @AllArgsConstructor
    public static class WorkbookData{
        private Long workbookId;
        private Integer examYear;
        private Integer no;
        private Integer totalQuestionCount;
        private Integer timeLimit;
        private WorkbookStatus workbookStatus;
        private String workbookStatusName;
        private AttemptStatus attemptStatus;
        private String attemptStatusName;
        private Boolean isPassed;
    }


}
