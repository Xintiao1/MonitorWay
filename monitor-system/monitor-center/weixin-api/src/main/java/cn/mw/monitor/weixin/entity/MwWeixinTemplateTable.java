package cn.mw.monitor.weixin.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("微信模板类")
public class MwWeixinTemplateTable {
    private Integer id;
    private  String templateId;
    private  String  title;
    private  String primaryIndustry;
    private  String deputyIndustry;
    private  String content;
    private  String example;
}
