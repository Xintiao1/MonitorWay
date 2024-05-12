package cn.mw.monitor.service.user.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author syt
 * @Date 2020/12/25 15:01
 * @Version 1.0
 */
@Data
@Builder
public class UpdateDTO {
    //是否修改用户
    private boolean isUser;
    //是否修改用户组
    private boolean isGroup;
    //是否修改机构
    private boolean isOrg;
    private List<Integer> groupIds; //用户组
    private List<Integer> userIds;  //责任人
    private List<List<Integer>> orgIds; //机构
    private String typeId; //资产数据的id
    private String type;  //ASSETD
    private String desc;  //资产

    private List<String> typeIds;
}
