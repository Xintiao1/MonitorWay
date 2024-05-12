package cn.mw.monitor.weixin.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
@ApiModel("微信关注用户基本对象类")
public class MwWeixinUserTable {

    @ApiModelProperty(name = "ID")
    private Integer id;

    @ApiModelProperty(name = "openid,该标识微信用户对每个服务号唯一")
    private String openid;

    @ApiModelProperty(name = "昵称")
    private String nickname;

    @ApiModelProperty(name = "性别")
    private Integer sex;

    @ApiModelProperty(name = "国家")
    private String country;

    @ApiModelProperty(name = "省")
    private String province;

    @ApiModelProperty(name = "城市")
    private String city;

    private String creator;

    private Date createDate;

    private String modifier;

    private Date modificationDate;

}
