package cn.mw.monitor.service.assets.model;

import cn.mw.monitor.bean.BaseParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2020/6/20 17:11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MwCommonAssetsDto extends MwManagerDto {
    private String loginName;
    private String perm;
    private Integer userId;
    private List<Integer> groups;
    private Integer assetsTypeId;
    private String assetsId;
    private String assetsName;
    private String inBandIp;
    private Integer assetsTypeSubId;
    private String pollingEngine;
    private Integer monitorMode;
    private String manufacturer;
    private String specifications;
    private String type;
    private Integer timeLag;
    private String modelDataId;
    private Integer modelId;
    @ApiModelProperty("资产是否已经选择了告警方式")
    private Boolean isAlert;
    private String filterOrgId;
    private String filterLabelId;
    private List<Integer> filterLabelIds;
    private List<List<Integer>> filterOrgIds;

    private Integer labelId;
    private Integer InputFormat;
    private String labelValue;
    private Integer dropKey;
    private Date labelDateStart;
    private Date labelDateEnd;

    private List<String> assetsIds;

    //是否忽略数据权限控制  true忽略，可在定时任务时设置为true，避免没有userId导致报错
    private Boolean skipDataPermission;

    //是否查询告警字段
    private boolean isAlertQuery;

    private Boolean isQueryAssetsState;

    private Boolean monitorFlag;

}
