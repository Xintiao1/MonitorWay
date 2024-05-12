package cn.mw.monitor.automanage.param;

import lombok.Data;

import java.util.List;

/**
 * @author gui.quanwang
 * @className NacosResponse
 * @description 请求nacos返回实体类
 * @date 2022/4/4
 */
@Data
public class NacosResponse {

    private String name;

    private String groupName;

    private List<NacosInstance> hosts;
}
