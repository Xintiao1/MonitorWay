package cn.mw.monitor.user.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Created by zy.quaee on 2021/9/1 10:41.
 **/
@Data
@Builder
public class MwTempUserDTO {

    private Integer userId;
    private String creator;
}
