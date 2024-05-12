package cn.mw.monitor.user.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Created by zy.quaee on 2021/9/1 10:43.
 **/
@Data
@Builder
public class MwTempConfigDTO {

    private Integer configId;
    private String creator;
}
