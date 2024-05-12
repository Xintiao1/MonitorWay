package cn.mw.monitor.service.ipmanage.model;

import lombok.Data;

@Data
public class IPInfoChangeView {
    private String ip;

    private String oldMac;

    private String newMac;
}
