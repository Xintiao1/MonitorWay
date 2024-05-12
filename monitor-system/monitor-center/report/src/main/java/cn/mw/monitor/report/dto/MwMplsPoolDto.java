package cn.mw.monitor.report.dto;

import cn.mwpaas.common.model.Reply;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName MwMplsPoolDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/11/22 11:04
 * @Version 1.0
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MwMplsPoolDto {
    private List historyData;

    private MwMplsPoolReportDto linePool;

}
