package cn.mw.monitor.service.assets.utils;

public enum VersionType {
    SNMPv1(0, 1)
    , SNMPv2(1, 2)
    ;

    private int snmpVersion;

    private int zabbixVersion;


    VersionType(int snmpVersion, int zabbixVersion){
        this.snmpVersion = snmpVersion;
        this.zabbixVersion = zabbixVersion;
    }

    public int getSnmpVersion() { return this.snmpVersion; }

    public int getZabbixVersion() { return this.zabbixVersion; }

    public static VersionType getZabbixVersionBySnmpVersion(int snmpVersion) {
        for(VersionType versionType : VersionType.values()) {
            if(versionType.getSnmpVersion() == snmpVersion) {
                return versionType;
            }
        }
        return null;
    }
}
