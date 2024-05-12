package cn.mw.monitor.credential.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Created by zy.quaee on 2021/5/31 16:58.
 **/
@Data
@Builder
public class MwSysCredDTO {
    /**
     * 账号
     */
    @ApiModelProperty(value="账号")
    private String account;

    /**
     * 密码
     */
    @ApiModelProperty(value="密码")
    private String passwd;
}
