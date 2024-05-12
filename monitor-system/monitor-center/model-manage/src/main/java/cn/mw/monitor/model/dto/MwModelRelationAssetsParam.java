package cn.mw.monitor.model.dto;

import cn.mw.monitor.service.assets.param.QueryAssetsInterfaceParam;
import cn.mw.monitor.service.model.dto.BaseModelInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author baochengbin
 * @date 2020/03/16
 *
 */
@Data
public class MwModelRelationAssetsParam {

    /**
     * 资产id
     */
    private String assetsId;

    /**
     * 资产名称
     */
    private String assetsName;

    /**
     * 主机名称
     */
    private String hostName;

    /**
     * 带内IP
     */
    private String inBandIp;


    /**
     * 厂商
     */
    private String manufacturer;

    /**
     * 规格型号
     */
    private String specifications;

    /**
     * 描述
     */
    private String description;

    //资产管理的数据
    private List relationListData;

    private String modelIndex;

    private Integer modelId;

    private Integer modelInstanceId;
}
