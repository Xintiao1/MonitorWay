package cn.mw.monitor.labelManage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MwLabelAssetsTypeMapper {

    // 主键
    private Integer id;
    // 标签id
    private Integer labelId;
    // 资产类型id
    private Integer assetsTypeId;
    // 更新时间
    private Date updateTime;
    // 删除标识
    private Boolean deleteFlag;

}
