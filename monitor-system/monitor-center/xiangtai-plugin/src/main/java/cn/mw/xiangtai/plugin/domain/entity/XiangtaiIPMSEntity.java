package cn.mw.xiangtai.plugin.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("xiangtai_ipms")
public class XiangtaiIPMSEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("mac_address")
    private String macAddress;

    @TableField("first_online")
    private Date firstOnline;

    @TableField("last_online")
    private Date lastOnline;

    @TableField("device_alias")
    private String deviceAlias;

    @TableField("device_type")
    private String deviceType;

    @TableField("user_name")
    private String userName;

    @TableField("responsible_person")
    private String responsiblePerson;

    @TableField("contact_number")
    private String contactNumber;

    @TableField("company_department")
    private String companyDepartment;

    @TableField("installation_location")
    private String installationLocation;

    @TableField("vendor")
    private String vendor;

    @TableField("notes")
    private String notes;
}
