package cn.mw.monitor.alert.param;

import cn.mw.monitor.bean.DataPermissionParam;
import cn.mw.monitor.state.DataType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2020/8/30 10:54
 */
@Data
public class AlertActionParam extends DataPermissionParam {
    private String actionId;
    private String actionName;
    @ApiModelProperty("是否是当前登录者的所有默认的资产")
    private Boolean isAllAssets;
    @ApiModelProperty("是否是当前登录者的所有可选的用户")
    private Boolean isAllUser;
    private String creator;
    private String modifier;
    private Date createDateStart;
    private Date createDateEnd;
    private Date modificationDateStart;
    private Date modificationDateEnd;
    private List<Integer> userIds;
    private List<Integer> groupIds;
    private Integer userId;
    private Boolean isAdmin;
    private Boolean enable;
    private String effectTimeSelect;

    @Override
    public DataType getBaseDataType() {
        return DataType.ACTION;
    }

    @Override
    public String getBaseTypeId() {
        return actionId + "";
    }
}
