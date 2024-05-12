package cn.mw.monitor.report.dto;

import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.bean.BaseParam;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mwpaas.common.utils.Md5Utils;
import cn.mwpaas.common.utils.StringUtils;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

/**
 * @author xhy
 * @date 2020/5/4 10:34
 */
@Data
public class TrendParam extends QueryTangAssetsParam {
    @ApiModelProperty("报表id")
    private Integer reportId;

    @ApiModelProperty("时间类型，1：hour 2:day 3:week 4:month")
    private Integer dateType;
    @ApiModelProperty("资产类型ID")
    private Integer assetsTypeId;

    @ApiModelProperty(" 0 全天(24小时) 1全天(自定义时间段） 2 工作日（24小时） 3工作日（自定义时间段） ")
    private Integer dayType;

    @ApiModelProperty("是否高级查询 true 是")
    private Boolean seniorchecked;

    @ApiModelProperty("非高级查询选择的日期")
    private List<String> fixedDate;

    //高级查询对应的字段
    @ApiModelProperty("选择的日期")
    private List<String> chooseTime;

    //资产名称
    private String assetsName;
    //资产类型名称
    private String assetsTypeName;
    //更新日期
    private Date modificationDateStart;
    //更新日期
    private Date modificationDateEnd;
    //厂商
    private String manufacturer;
    //规格型号
    private String specifications;
    //设施分类标识符
    private String assetsTypeSubName;
    //在用状态
    private Boolean monitorFlag;
    private Long startTime;
    private Long endTime;
    private List<MwTangibleassetsTable> mwTangibleassetsDTOS;


    //zabbix线路报表相关参数

    private String interfaceID;

    private String caption;

    @ApiModelProperty("工作日 全天")
    private String periodRadio;

    @ApiModelProperty("最大值，最小值，平均值")
    private String valueType;

    private String mouthTime;

    private List<String> interfaceIds;
    //颗粒度（分钟）
    private String particle;
    //资产类型具体ip
    private String assertIp;
    //源端口
    private String rootPort;
    //目标端口
    private String targetPort;

    private List<String> ids;

    //线路类型
    private Integer lineType;

    //是否定时任务类型
    private Boolean timingType;

    private int reportType;

    private List<String> interFaceNames;

    public String getRedisKey() throws Exception{
        StringBuffer sb = new StringBuffer();
        Field[] fields =this.getClass().getDeclaredFields();
        for(Field field : fields){
            field.setAccessible(true);
            if("mwTangibleassetsDTOS".equals(field.getName())){
                continue;
            }

            String type = field.getGenericType().getTypeName();
            if("java.util.List<java.lang.String>".equals(type)){
                List<String> datas = (List<String>)field.get(this);
                if(null != datas && datas.size() > 0){
                    for(String data : datas){
                        String str = data.split(" ")[0];
                        sb.append(str);
                    }
                }
            }else {
                sb.append(field.get(this));
            }
        }

        return this.getClass().getSimpleName() + ":" + Md5Utils.encode(sb.toString());
    }

}
