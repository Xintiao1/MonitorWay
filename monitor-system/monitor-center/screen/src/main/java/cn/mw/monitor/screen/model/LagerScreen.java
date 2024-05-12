package cn.mw.monitor.screen.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author xhy
 * @date 2020/4/10 15:25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LagerScreen implements Serializable {
    private static final long serialVersionUID = -6526080305101583934L;
    private String screenId;
    private String screenName;
    private String screenDesc;
    private String enable;
    private String createUser;
    private String createDate;
    private String updateUser;
    private String updateDate;
    private Integer userId;//是否有用户
    private Integer groupId;//是否有用户组
}
