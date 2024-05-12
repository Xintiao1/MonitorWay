package cn.mw.monitor.service.user.model;

import cn.mw.monitor.service.user.dto.MwSubUserDTO;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class MWOrg {

    // 机构ID
    private Integer orgId;
    // 机构名称
    private String orgName;
    // 地址
    private String address;
    // 邮政编码
    private String postCode;
    // 联系人
    private String contactPerson;
    // 联系电话
    private String contactPhone;
    // 机构描述
    private String orgDesc;
    // 组织类型id
    private String orgType;
    // 深度
    private Integer deep;
    // 机构父ID
    private Integer pid;
    // 节点ID
    private String nodes;
    // 是否子节点
    private Boolean isNode;
    // 状态
    private String enable;
    // 创建人
    private String creator;
    // 创建时间
    private Date createDate;
    // 修改人
    private String modifier;
    // 修改时间
    private Date modificationDate;
    // 删除标识
    private Boolean deleteFlag;

    //状态返回标识  active-- false   disactive-- true
    private Boolean disabledFlag;

    // 用户列表
    private List<MwSubUserDTO> userDTOS;

    //经纬度
    private String coordinate;

}
