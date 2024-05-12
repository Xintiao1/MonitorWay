package cn.mw.monitor.configmanage.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class MwAccountMapper {

    //自增序列
    private Integer id;
    //账号
    private String account;
    //账号主键
    private Integer accountId;
    //资产主键
    private String assetsId;

    @ApiModelProperty("创建时间")
    private Date createDate;
    @ApiModelProperty("创建人")
    private String creator;
    @ApiModelProperty("修改时间")
    private Date modificationDate;
    @ApiModelProperty("修改人")
    private String modifier;

    private int delay=200;

}
