package cn.mw.monitor.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author zy.quaee
 * @date 2021/5/20 9:47
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ADUserDetailDTO {
    private Integer id;

    private String userName;

    private String groupName;

    private String loginName;

    private Boolean needAdd;

    private String mail;

    private String phone;

    /**
     * 2023-03-27 用于更新用户微信号
     */
    private String wxNo;

    /**
     * 2023-03-27 用于更新用户钉钉号
     */
    private String dingdingNo;

    /**
     * 2023-03-27 TCL项目，用于更新用户状态
     */
    private String enabled;

    /**
     * 2023-03-30 TCL项目，用于更新科长属性（上级）
     */
    private String manager;
}
