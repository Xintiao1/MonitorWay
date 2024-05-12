package cn.mw.monitor.server.param;

import lombok.Data;

import java.util.List;

/**
 * @ClassName MwOpenItemParam
 * @Description ToDo
 * @Author gengjb
 * @Date 2023/4/11 12:03
 * @Version 1.0
 **/
@Data
public class MwOpenItemParam {

    private String monitorServerId;

    private String assetsId;

    private String itemName;

    private List<String> hostIds;

    private List<String> itemNames;
}
