package cn.mw.monitor.service.assets.model;

import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @author guiquanwnag
 * @datetime 2023/5/15
 * @Description 简化标签数据
 */
@Data
public class SimplifyLabelDTO {

    /**
     * 标签ID
     */
    private int labelId;

    /**
     * 标签名称
     */
    private String labelName;

    /**
     * 标签值列表
     */
    private Set<String> tagBoardList;

    /**
     * 输入框类型
     */
    private String inputFormat;

}
