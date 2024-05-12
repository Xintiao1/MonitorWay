package cn.mw.zbx.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Objects;

/**
 * @author xhy
 * @date 2020/4/24 17:42
 */
@Data
@Builder
public class MWWebDto {
    @ApiModelProperty("httptestids")
    private String httptestids;
    @ApiModelProperty("web名称")
    private String name;
    @ApiModelProperty("绑定的主机id")
    private String hostId;
    @ApiModelProperty("代理人 默认Zabbix")
    private String agent;
    @ApiModelProperty("延迟时间 1m")
    private String delay;
    @ApiModelProperty("是否启用web 0启用 1禁用")
    private Integer status;
    @ApiModelProperty("web失败重试次数")
    private Integer retries;//web失败重试次数
    @ApiModelProperty("步骤")
    private List<MWStep> steps;
    @ApiModelProperty("代理")
    private String httpProxy;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MWWebDto mwWebDto = (MWWebDto) o;
        return Objects.equals(httptestids, mwWebDto.httptestids) &&
                Objects.equals(name, mwWebDto.name) &&
                Objects.equals(hostId, mwWebDto.hostId) &&
                Objects.equals(agent, mwWebDto.agent) &&
                Objects.equals(delay, mwWebDto.delay) &&
                Objects.equals(status, mwWebDto.status) &&
                Objects.equals(retries, mwWebDto.retries) &&
                Objects.equals(steps, mwWebDto.steps) &&
                Objects.equals(httpProxy, mwWebDto.httpProxy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(httptestids, name, hostId, agent, delay, status, retries, steps, httpProxy);
    }
}
