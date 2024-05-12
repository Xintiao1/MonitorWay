package cn.mw.monitor.ipaddressmanage.param;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.Date;

/**
 * @author bkc
 * @date 2020/12/10
 */
@Data
@ApiModel("OUI")
public class MwOUIParam {

    //mac地址前六位
    private String mac;

    //厂商
    private String vendor;

    //厂商地址
    private String address;

    //厂商简称（许多厂商没有简称）
    private String shortName;


}
