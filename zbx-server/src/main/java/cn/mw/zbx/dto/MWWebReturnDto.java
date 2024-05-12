package cn.mw.zbx.dto;

import lombok.Data;

/**
 * @author xhy
 * @date 2020/4/25 16:05
 */
@Data
public class MWWebReturnDto {
    //web监测zibbix生成的id
    private String httptestid;
    //名称
    private String name;
    //
    private String applicationid;
    //
    private String nextcheck;
    //延迟
    private String delay;
    //状态(0.启用，1.未启用)
    private String status;
    //客户端（zibbix）
    private String agent;
    //资产id
    private String hostid;
    //重试次数
    private String retries;
}
