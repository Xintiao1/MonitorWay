package cn.mw.monitor.service.netflow.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author guiquanwnag
 * @datetime 2023/8/28
 * @Description 返回结果
 */
@Data
public class NetflowResult implements Serializable {

    /**
     * 资产ID
     */
    private String assetsId;

    /**
     * 入向流量总量（总量+单位，例如10Kb）
     */
    private String inSum;

    /**
     * 出向流量总量（总量+单位，例如10Kb）
     */
    private String outSum;

    /**
     * 入向流量峰值（总量+单位，例如10Kb）
     */
    private String inMax;

    /**
     * 出向流量峰值（总量+单位，例如10Kb）
     */
    private String outMax;


}
