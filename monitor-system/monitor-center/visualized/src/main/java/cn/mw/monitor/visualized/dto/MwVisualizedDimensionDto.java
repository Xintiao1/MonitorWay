package cn.mw.monitor.visualized.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName MwVisualizedDimensionDto
 * @Description 可视化维度数据实体
 * @Author gengjb
 * @Date 2022/4/20 14:27
 * @Version 1.0
 **/
@Data
public class MwVisualizedDimensionDto {
    //资产类型/标签/厂商/资产子类型/标签属性/规格型号id/用户组/用户/机构
    private int typeId;
    //资产类型/标签/厂商/资产子类型/标签属性/规格型号名称/用户组/用户/机构
    private String typeName;
    //资产数量
    private int count;
    //资产id 数组
    private List<MwVisualizedAssetsDto> assetsList;

    //前面小图标名字信息
    private String url;

    //前面小图标从何取值的状态值
    private int url_type;
    //状态的图标
    private String statusUrl;

    // 用于前端特殊展示的唯一标识
    private String uuid;

    //用于标签辨别标签样式
    private int inputFormat;

    private List<MwVisualizedDimensionDto> children;

    /**
     * 图标类型： 0-系统定义，1-用户上传
     */
    private Integer vendorCustomFlag;

    public void addChild(MwVisualizedDimensionDto visualizedDimensionDto) {
        if (null == children) {
            children = new ArrayList<MwVisualizedDimensionDto>();
        }
        children.add(visualizedDimensionDto);
    }
}
