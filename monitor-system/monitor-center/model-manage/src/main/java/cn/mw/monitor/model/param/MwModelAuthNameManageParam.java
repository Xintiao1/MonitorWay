package cn.mw.monitor.model.param;

import cn.mw.monitor.model.dto.MwModelMacrosManageDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2023/2/19
 */
@Data
@ApiModel
public class MwModelAuthNameManageParam {
    @ApiModelProperty("凭证名称")
    private String authName;
    private List<MwModelMacrosManageDTO> macrosParam;
    private List<Integer> modelGroupIdList;

}
