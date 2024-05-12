package cn.huaxing.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

/**
 * @author gengjb
 * @description 文件趋势
 * @date 2023/9/6 9:49
 */
@Data
@ApiModel("文件趋势")
public class HuaxingVisualizedFileTrendDto {

    private String name;

    private String time;

    private String count;

    private Date sortTime;
}
