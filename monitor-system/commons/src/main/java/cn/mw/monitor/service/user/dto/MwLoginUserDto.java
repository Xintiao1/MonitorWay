package cn.mw.monitor.service.user.dto;

import lombok.Data;

/**
 * @ClassName MwLoginUserDto
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/10/8 11:15
 * @Version 1.0
 **/
@Data
public class MwLoginUserDto {

    private String loginName;

    private Integer userId;

    private Integer roleId;

    private String dataPerm;
}
