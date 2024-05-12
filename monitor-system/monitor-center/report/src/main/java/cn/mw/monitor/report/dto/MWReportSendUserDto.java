package cn.mw.monitor.report.dto;

import lombok.Data;

/**
 * @ClassName MWReportSendUserDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/1/26 11:45
 * @Version 1.0
 **/
@Data
public class MWReportSendUserDto {

    private String reportId;

    private int userId;

    private int groupId;

    private String type;
//
//    private String userName;
//
//    private String loginName;
//
//    private String groupName;
}
