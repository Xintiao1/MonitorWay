package cn.mw.monitor.service.model.dto;

import lombok.Data;

import java.util.Map;

/**
 * @ClassName DetailPageJumpDto
 * @Description 跳转参数
 * @Author gengjb
 * @Date 2023/2/12 15:07
 * @Version 1.0
 **/
@Data
public class DetailPageJumpDto {

    //跳转链接
    private String url;

    //参数数据
    private Map param;
}
