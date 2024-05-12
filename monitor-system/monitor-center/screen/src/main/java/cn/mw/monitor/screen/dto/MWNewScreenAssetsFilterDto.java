package cn.mw.monitor.screen.dto;

import cn.mw.monitor.weixinapi.MwRuleSelectParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @ClassName MWNewScreenAssetsFilterDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/1/13 15:07
 * @Version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MWNewScreenAssetsFilterDto {
    //过滤资产表ID
    private Integer id;

    //卡片名称
    private String name;

    //组件ID
    private String modelDataId;

    //模块ID
    private int modelId;

    //用户ID
    private int userId;

    //资产名称
    private String assetsName;

    //IP
    private String inBandIp;

    //资产类型
    private Integer assetsTypeId;

    //资产子类型
    private Integer assetsTypeSubId;

    //监控方式
    private int monitorMode;

    //轮询引擎
    private String pollingEngine;

    //厂商
    private String manufacturer;

    //规格型号
    private String specifications;

    //刷新间隔
    private int timeLag;

    //标签ID
    private String filterLabelId;

    //机构ID
    private String filterOrgId;

    private String bulkName;

    private List<MwRuleSelectParam> labelDtos;
}
