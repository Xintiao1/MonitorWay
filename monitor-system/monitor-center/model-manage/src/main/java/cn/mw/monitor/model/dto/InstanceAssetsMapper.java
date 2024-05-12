package cn.mw.monitor.model.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author xhy
 * @date 2021/3/2 9:51
 */
@Builder
@Data
@ApiModel
@NoArgsConstructor
@AllArgsConstructor
public class InstanceAssetsMapper {
    private String tangibleId;
    private Integer modelInstanceId;
}
