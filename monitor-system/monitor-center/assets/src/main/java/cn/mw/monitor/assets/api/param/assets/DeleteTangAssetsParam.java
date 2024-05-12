package cn.mw.monitor.assets.api.param.assets;

import cn.mw.monitor.service.assets.param.DeleteTangAssetsID;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/16
 */
@Data
@ApiModel(value = "删除外带资产数据")
public class DeleteTangAssetsParam {

    @ApiModelProperty(value = "删除外带资产id列表")
    private List<DeleteTangAssetsID> idList;
}
