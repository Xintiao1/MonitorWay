package cn.mw.monitor.screen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName MWNewScreenLabelDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/1/21 10:14
 * @Version 1.0
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MWNewScreenLabelDto {

    //新首页标签过滤表ID
    private int id;

    //标签id
    private int labelId;

    //标签名称
    private String labelName;

    //标签状态
    private String labelStatus;

    //标签类型 1:文本 2：时间  3：下拉
    private int labelType;

    //标签值
    private String labelValue;

    //资产过滤id
    private int filterAssetsId;
}
