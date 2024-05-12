package cn.mw.monitor.alert.param;

import cn.mw.monitor.service.action.param.UserIdsType;
import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.HashSet;
import java.util.List;

/**
 * @author
 * @date
 */
@Data
public class TriggerInfoParam {
    @ExcelProperty(value = {"资产名称"},index = 0)
    private String assetsName;
    @ExcelProperty(value = {"IP地址"},index = 1)
    private String ip;
    @ExcelProperty(value = {"触发器描述"},index = 2)
    private String description;
    @ExcelProperty(value = {"触发器等级"},index = 3)
    private String level;
    @ExcelProperty(value = {"触发器表达式"},index = 4)
    private String expression;
    @ExcelProperty(value = {"监控服务器"},index = 5)
    private String monitorServerName;
    @ExcelIgnore
    private Integer monitorServerId;
    @ExcelIgnore
    private String triggerid;
    @ExcelIgnore
    private String hostid;



}
