package cn.mw.monitor.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @ClassName MWMplsCacheDataDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/12/9 10:26
 * @Version 1.0
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MWMplsCacheDataDto {

    private String linkName;

    private String send;

    private String accept;

    private Date saveTime;

    private String weekData;

    private String monthDate;

    //自动进程成功标识
    private boolean updateSuccess;
}
