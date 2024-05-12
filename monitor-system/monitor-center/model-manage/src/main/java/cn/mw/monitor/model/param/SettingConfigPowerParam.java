package cn.mw.monitor.model.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2021/2/23 17:10
 */
@Data
@ApiModel
public class SettingConfigPowerParam {
    private List<String> paramList;
}
