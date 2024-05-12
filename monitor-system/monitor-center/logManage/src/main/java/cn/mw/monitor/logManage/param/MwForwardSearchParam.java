package cn.mw.monitor.logManage.param;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class MwForwardSearchParam extends BaseParam implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fuzzyQuery;

    private String forwardingName;

    private String forwardingIp;

    private Integer status;
}
