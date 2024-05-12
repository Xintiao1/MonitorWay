package cn.mw.monitor.service.server.api.dto;

/**
 * @author qzg
 * @date 2021/7/2
 */

import cn.mw.monitor.bean.BaseParam;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 高级表格查询
 *
 * @author qzg
 * @Date 2021/6/20
 */
@Data
public class AdvanceTableDTO extends BaseParam {

    @ApiModelProperty("组件对应的基础信息，必填")
    private AssetsBaseDTO assetsBaseDTO;

    @ApiModelProperty("监控服务器id")
    private Integer monitorServerId;
    @ApiModelProperty("hostid")
    private String hostid;
    @ApiModelProperty("应用集名称集合")
    private List<ApplicationDTO> applicationList;
    @ApiModelProperty("页面布局Id")
    private Integer comLayoutId;
    @ApiModelProperty("组件布局")
    private String componentLayout;
    //查询条件字段名
    private String queryName;
    //查询条件字段值
    private String queryValue;

    @ApiModelProperty("排序的属性名称")
    private String sortField;
    @ApiModelProperty("0为升序；1为倒序")
    private Integer sortMode;

    @ApiModelProperty("资产状态")
    private String assetsStatus;

}
