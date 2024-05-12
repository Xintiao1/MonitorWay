package cn.mw.monitor.screen.dto;

import cn.mw.monitor.service.user.dto.OrgDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2020/4/12 15:54
 */
@Data
public class LagerScreenDataDto {
    private String screenId;
    private String layoutDataId;
    private String screenName;
    private String screenDesc;
    private String enable;
    private String image;
    private Integer layoutId;
    private List<ModelDto> modelDtos;
    @ApiModelProperty(value="机构")
    private List<List<Integer>> orgIds;

    private List<OrgDTO> department;

    @ApiModelProperty(value="负责人")
    private List<Integer> userIds;

    @ApiModelProperty(value="用户组")
    private List<Integer> groupIds;

    private Date createDate;

}
