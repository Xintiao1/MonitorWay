package cn.mw.monitor.visualized.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @ClassName
 * @Description 机器信息DTO
 * @Author gengjb
 * @Date 2023/5/19 15:44
 * @Version 1.0
 **/
@Data
@ApiModel("机器信息DTO")
public class MwVisualizedModuleMacheineDto {

    private String name;

    private String value;

    private String units;

}
