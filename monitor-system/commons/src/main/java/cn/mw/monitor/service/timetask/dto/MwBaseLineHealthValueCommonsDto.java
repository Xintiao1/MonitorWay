package cn.mw.monitor.service.timetask.dto;

import lombok.Builder;
import lombok.Data;

/**
 * @ClassName MwBaseLineHealthValueCommonsDto
 * @Description 基线健康值实体
 * @Author gengjb
 * @Date 2022/5/30 9:53
 * @Version 1.0
 **/
@Data
@Builder
public class MwBaseLineHealthValueCommonsDto {

    /**
     * 健康值ID
     */
    private Integer id;

    /**
     * 资产主机ID
     */
    private String assetsId;

    /**
     * 监控项名称
     */
    private String itemName;

    /**
     * 值
     */
    private String value;

    private String assetsName;
}
