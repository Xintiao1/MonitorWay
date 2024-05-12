package cn.mw.monitor.service.scan.param;

import cn.mw.monitor.service.assets.model.MwSnmpAssetsDTO;
import cn.mw.monitor.service.assets.model.MwSnmpv1AssetsDTO;
import cn.mw.monitor.service.assets.utils.RuleType;
import cn.mw.monitor.service.scan.dto.MwTopoGraphSnmpv1v2DTO;
import cn.mw.monitor.service.scan.dto.MwTopoGraphSnmpv3DTO;
import cn.mw.monitor.service.scan.model.ScanResultSuccess;
import lombok.Data;

@Data
public class RuleParam {
    private String ruleType;
    private String port;
    private String community;
    private String oid;
    private String contextName;
    private String securityName;
    private String securityLevel;
    private String authProtocol;
    private String authToken;
    private String privProtocol;
    private String privToken;

    public String debugInfo(){
        StringBuffer sb = new StringBuffer();
        sb.append(this.ruleType).append(";")
                .append(port).append(";")
                .append(oid);
        return sb.toString();
    }

    public RuleParam(){

    }

    public RuleParam(ScanResultSuccess scanResultSuccess){
        this.ruleType = RuleType.valueOf(scanResultSuccess.getMonitorMode()).getName();
        this.port = scanResultSuccess.getPort();
        this.community = scanResultSuccess.getCommunity();
        this.contextName = scanResultSuccess.getContextName();
        this.securityName = scanResultSuccess.getSecurityName();
        if(null != scanResultSuccess.getSecurityLevel()) {
            this.securityLevel = scanResultSuccess.getSecurityLevel().getName();
        }

        if(null != scanResultSuccess.getAuthProtocol()) {
            this.authProtocol = scanResultSuccess.getAuthProtocol().getName();
        }
        this.authToken = scanResultSuccess.getAuthToken();

        if(null != scanResultSuccess.getPrivProtocol()) {
            this.privProtocol = scanResultSuccess.getPrivProtocol().getName();
        }
        this.privToken = scanResultSuccess.getPrivToken();
    }

    public void extractFromMwSnmpv1AssetsDTO(MwSnmpv1AssetsDTO mwSnmpv1AssetsDTO){

        this.setRuleType(RuleType.SNMPv1v2.getName());
        this.setPort(mwSnmpv1AssetsDTO.getPort().toString());
        this.setCommunity(mwSnmpv1AssetsDTO.getCommunity());
    }

    public void extractFromMwSnmpAssetsDTO(MwSnmpAssetsDTO mwSnmpAssetsDTO){
        this.setRuleType(RuleType.SNMPv3.getName());
        this.setPort(mwSnmpAssetsDTO.getPort().toString());
        this.setSecurityName(mwSnmpAssetsDTO.getSecName());
        this.setContextName(mwSnmpAssetsDTO.getContextName());
        this.setSecurityLevel(mwSnmpAssetsDTO.getSecLevelName());
        this.setAuthProtocol(mwSnmpAssetsDTO.getAuthAlgName());
        this.setAuthToken(mwSnmpAssetsDTO.getAuthValue());
        this.setPrivProtocol(mwSnmpAssetsDTO.getPrivAlgName());
        this.setPrivToken(mwSnmpAssetsDTO.getPriValue());
    }

    public void extractFromMwTopoGraphSnmpv1v2DTO(MwTopoGraphSnmpv1v2DTO mwTopoGraphSnmpv1v2DTO){
        this.setPort(mwTopoGraphSnmpv1v2DTO.getPort());
        this.setRuleType(RuleType.SNMPv1v2.getName());
        this.setCommunity(mwTopoGraphSnmpv1v2DTO.getCommunity());
    }

    public void extractFromMwTopoGraphSnmpv3DTO(MwTopoGraphSnmpv3DTO mwTopoGraphSnmpv3DTO){
        this.setPort(mwTopoGraphSnmpv3DTO.getPort());
        this.setRuleType(RuleType.SNMPv3.getName());
        this.setSecurityName(mwTopoGraphSnmpv3DTO.getSecName());
        this.setContextName(mwTopoGraphSnmpv3DTO.getContextName());
        this.setSecurityLevel(mwTopoGraphSnmpv3DTO.getSecLevel());
        this.setAuthProtocol(mwTopoGraphSnmpv3DTO.getAuthAlg());
        this.setAuthToken(mwTopoGraphSnmpv3DTO.getAuthValue());
        this.setPrivProtocol(mwTopoGraphSnmpv3DTO.getPrivAlg());
        this.setPrivToken(mwTopoGraphSnmpv3DTO.getPriValue());
    }
}
