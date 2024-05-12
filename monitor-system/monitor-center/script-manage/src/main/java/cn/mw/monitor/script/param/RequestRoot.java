package cn.mw.monitor.script.param;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author gui.quanwang
 * @className RequestRoot
 * @description 请求账户
 * @date 2022/4/14
 */
@Data
public class RequestRoot implements Serializable {

    private static final long serialVersionUID = 1L;

    private String admin;

    private String pwd;

    private Integer prot=22;

    private Integer type=1;

    private String mysqlRoot;

    private String mysqlPwd;

}
