package cn.mw.monitor.service.timetask.dto;


import lombok.Data;

import java.util.List;

/**
 * @ClassName MwBaseLineHealthValueCommonsDto
 * @Description 基线健康值实体
 * @Author gengjb
 * @Date 2022/5/30 9:53
 * @Version 1.0
 **/
@Data
public class MwBaseLineHealthValueResultParam {

    private String assetsName;

    private List<MwBaseLineHealthValueCommonsParam> assetsNames;
}
