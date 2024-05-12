package cn.mw.monitor.user.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @Author shenwenyi
 * @Date 2023/8/9 12:23
 * @PackageName:cn.mw.monitor.user.model
 * @ClassName: UserSessionDTO
 * @Description: TODD
 * @Version 1.0
 */
@Data
public class UserSessionDTO {


    private Integer userId;

    @ExcelProperty(value = "用户名", index = 0)
    private String userName;

    private String orgId;

    @ExcelProperty(value = "组织名称", index = 1)
    private String orgName;

    @ExcelProperty(value = "日期", index = 2)
    private String createTime;

    @ExcelProperty(value = "在线时长", index = 3)
    private String onlineTime;

    private Long totalOnlineTime;

    public void convertSecondsToTimeStr(){
        long seconds = totalOnlineTime;
        long hours = seconds / (60 * 60);
        seconds %= (60 * 60);
        long minutes = seconds / 60;
        seconds %=60;
        onlineTime =  hours +"小时"+minutes+"分"+seconds+"秒";
    }
}
