package cn.mw.monitor.server.serverdto;

import lombok.Data;

import java.util.List;

/**
 * @author xhy
 * @date 2020/5/2 15:37
 */
@Data
public class SysDto {
    private List<String> orgName;
    private List<String> groupNameList;
    private List<String> userNameList;
}
