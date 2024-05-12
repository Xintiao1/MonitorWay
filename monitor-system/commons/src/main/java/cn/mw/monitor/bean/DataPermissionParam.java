package cn.mw.monitor.bean;

import cn.mw.monitor.state.DataType;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author gui.quanwang
 * @className DataPermissionParam
 * @description 数据权限集成类
 * @date 2022/4/29
 */
@Data
@ApiModel
public abstract class DataPermissionParam extends BaseParam {

    /**
     * 绑定机构列表
     */
    @ApiModelProperty(value = "机构")
    @TableField(exist = false)
    private List<List<Integer>> orgIds;

    /**
     * 绑定用户组列表
     */
    @ApiModelProperty(value = "用户组")
    @TableField(exist = false)
    private List<Integer> groupIds;

    /**
     * 绑定责任人列表
     */
    @ApiModelProperty(value = "责任人")
    @TableField(exist = false)
    private List<Integer> principal;

    /**
     * 待删除ID列表（内部使用）
     */
    @TableField(exist = false)
    private List<String> deleteIdList;

    /**
     * 获取数据类别
     *
     * @return
     */
    public abstract DataType getBaseDataType();

    /**
     * 获取绑定的数据ID
     *
     * @return
     */
    public abstract String getBaseTypeId();
}
