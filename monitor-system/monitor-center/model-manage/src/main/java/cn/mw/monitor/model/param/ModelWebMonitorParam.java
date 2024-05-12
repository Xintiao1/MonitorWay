package cn.mw.monitor.model.param;

import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

/**
 * @author syt
 * @Date 2021/7/15 20:42
 * @Version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "导入webmonitor数据")
public class ModelWebMonitorParam {
    private String errorMessage;
    private List<MwModelImportWebMonitorParam> importWebList;
}
