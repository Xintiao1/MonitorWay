package cn.mw.monitor.service.model.param;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/4/24
 */
@Data
@ApiModel
public class SaveCabinetLayoutDataParam {
    private Integer start;
    private Integer end;
    private Boolean isUsed;
    private QueryAssetsListParam info;
    private String esId;
    //视图类型（默认视图，刀箱视图，刀片视图）
    private String type;
    //刀箱布局数据，二维数组，几行几列
    private List<List<SaveBladeInstanceParam>> daoData;
    //刀箱布局列数
    private Integer bayCol;
    //刀箱布局行数
    private Integer bayRow;
}
