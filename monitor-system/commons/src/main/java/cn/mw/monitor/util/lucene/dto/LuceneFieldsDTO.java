package cn.mw.monitor.util.lucene.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author syt
 * @Date 2020/8/19 15:01
 * @Version 1.0
 */
@Data
public class LuceneFieldsDTO {

    @ApiModelProperty(name = "加密id")
    private String id;

    @ApiModelProperty(name = "知识标题")
    private String title;

    @ApiModelProperty(name = "知识类型id")
    private String typeId;

    @ApiModelProperty(name = "触发原因")
    private String triggerCause;

    @ApiModelProperty(name = "解决方案")
    private String solution;

    public LuceneFieldsDTO(String id, String title, String typeId, String triggerCause, String solution) {
        this.id = id;
        this.title = title;
        this.typeId = typeId;
        this.triggerCause = triggerCause;
        this.solution = solution;
    }

    public LuceneFieldsDTO() {
    }
}
