package cn.mw.monitor.service.scan.param;

import cn.mw.monitor.bean.DataPermissionParam;
import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import cn.mw.monitor.service.scan.model.TopoMetaInfoType;
import cn.mw.monitor.state.DataType;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class TopoMetaInfoParam extends DataPermissionParam {
    private String topoId;
    private String topoName;
    private String type;


    /**
     * 描述
     */
    @ApiModelProperty(value = "描述")
    private String desc;

    /**
     * 标签信息
     */
    private List<MwAssetsLabelDTO> labelList;

    /**
     * 绑定责任人列表
     */
    @ApiModelProperty(value = "责任人")
    @TableField(exist = false)
    private List<Integer> respPerson;

    /**
     * 获取数据类别
     *
     * @return
     */
    @Override
    public DataType getBaseDataType() {
        TopoMetaInfoType infoType = TopoMetaInfoType.valueOf(type);
        switch (infoType) {
            case group:
                return DataType.TOPO_GROUP;
            case topo:
                return DataType.TOPO_GRAPH;
            default:
                return null;
        }
    }

    /**
     * 获取绑定的数据ID
     *
     * @return
     */
    @Override
    public String getBaseTypeId() {
        return topoId;
    }

    /**
     * 绑定责任人列表
     */
    @Override
    public List<Integer> getPrincipal() {
        return respPerson;
    }
}
