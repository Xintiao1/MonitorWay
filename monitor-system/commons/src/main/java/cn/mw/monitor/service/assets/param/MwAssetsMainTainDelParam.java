package cn.mw.monitor.service.assets.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @ClassName MwAssetsMainTainDelParam
 * @Description 删除维护的参数
 * @Author gengjb
 * @Date 2021/7/29 16:45
 * @Version 1.0
 **/
@Data
public class MwAssetsMainTainDelParam {

    @ApiModelProperty("维护ID")
    private List<Integer> maintenids;

    @ApiModelProperty("维护数据主键")
    private List<Integer> ids;

    private Integer maintenid;

    private Integer id;

    private Integer serverId;

    private List<MwAssetsMainTainDelParam> idList;
}
