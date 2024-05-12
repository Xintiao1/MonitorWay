package cn.mw.monitor.server.serverdto;

import cn.mw.monitor.util.SeverityUtils;
import cn.mw.monitor.util.UnitsUtil;
import cn.mwpaas.common.utils.StringUtils;
import lombok.Data;

/**
 * @author syt
 * @Date 2021/6/17 11:54
 * @Version 1.0
 * 监控项的名称规则一定是中括号在最前面，尖括号在后面，且尖括号不在中括号中
 */
@Data
public class ItemGetDTO {
    private String itemid;
    private String name;
    private String units;
    private String value_type;
    /** valuemapid = 137
     * 0 ⇒ Disconnected
     * 1 ⇒ Connected
     * 2 ⇒ ApprovalPending
     * 3 ⇒ UpgradingFirmware
     * 4 ⇒ Provisioning
     */
    private String valuemapid;
    private String lastvalue;

    //用于数字排序的
    private String sortName;
    private Double sortLastValue;

    private String hostid;


    //原始未处理过的[xxx]<xxx>
    private String originalType;
    //针对中括号中第一个值
    private String firstType;
    //针对中括号中第二个值
    private String secondType;
    //针对尖括号的值
    private String cuspType;

    private boolean lastValueFlag;

    public void setName(String name) {
        int index = name.indexOf("[");
        int index1 = name.indexOf(">");
        if (index != -1 && index1 != -1) {
            setOriginalType(name.substring(index, index1 + 1));

            this.cuspType = name.substring(name.indexOf("<") + 1, index1);
            this.name = name.substring(index1 + 1);

        } else if (index != -1 && index1 == -1) {
            setOriginalType(name.substring(index, name.indexOf("]") + 1));
            if (name.indexOf("]") == (name.length()-1)) {//分中括号在前在后
                this.name = name.substring(0, index);
            } else {
                this.name = name.substring(name.indexOf("]") + 1);
            }
        } else if (index == -1 && index1 != -1) {
            setOriginalType(name.substring(name.indexOf("<"), index1 + 1));
            this.cuspType = name.substring(name.indexOf("<") + 1, index1);
            if (name.indexOf(">") == (name.length()-1)) {//分尖括号在前在后
                this.name = name.substring(0, index1);
            } else {
                this.name = name.substring(index1 + 1);
            }
        } else {
            this.name = name;
        }
    }

    public void setOriginalType(String originalType) {
        if (originalType != null && StringUtils.isNotEmpty(originalType)) {
            String substring = originalType.substring(originalType.indexOf("[") + 1, originalType.indexOf("]"));
            if (substring.indexOf(",") != -1) {
                this.firstType = substring.substring(0, substring.indexOf(","));
                this.secondType = substring.substring(substring.indexOf(",") + 1);
            } else {
                this.firstType = substring;
            }
        }
        this.originalType = originalType;
    }

    public void setValuemapid(String valuemapid) {
        if ("137".equals(valuemapid)) {
            this.lastValueFlag = true;
        }
        this.valuemapid = valuemapid;
    }

    public void setLastvalue(String lastvalue) {
        if (lastvalue != null && StringUtils.isNotEmpty(lastvalue)) {
            if (this.lastValueFlag) {
                int i = Integer.parseInt(lastvalue);
                switch (i) {
                    case 0:
                        lastvalue = "Disconnected";
                        break;
                    case 1:
                        lastvalue = "Connected";
                        break;
                    case 2:
                        lastvalue = "ApprovalPending";
                        break;
                    case 3:
                        lastvalue = "UpgradingFirmware";
                        break;
                    case 4:
                        lastvalue = "Provisioning";
                        break;
                    default:
                        break;
                }
            } else {
                if (!"0".equals(this.valuemapid)) {

                } else {
                    if ("uptime".equals(this.units)) {
                        double v = Double.parseDouble(lastvalue);
                        setSortLastValue(v);
                        setSortName("sort" + this.name);
                        long l = new Double(v).longValue();
                        lastvalue = SeverityUtils.getLastTime(l);
                    } else if ("0".equals(this.value_type) || "3".equals(this.value_type)) {
                        double v = Double.parseDouble(lastvalue);
                        setSortLastValue(v);
                        setSortName("sort" + this.name);
                        lastvalue = UnitsUtil.getValueWithUnits(lastvalue, this.units);
                    } else {
                    }
                }
            }

        }
        this.lastvalue = lastvalue;
    }

}
