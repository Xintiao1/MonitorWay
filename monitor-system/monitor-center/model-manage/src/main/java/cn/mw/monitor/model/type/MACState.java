package cn.mw.monitor.model.type;

/**
 * @author guiquanwnag
 * @datetime 2023/7/2
 * @Description MACState
 */
public enum MACState {

    OTHER(1, "other"),
    INVALID(2, "invalid"),
    LEARNED(3, "learned"),
    SELF(4, "self"),
    MGMT(5, "mgmt");

    private int type;

    private String name;

    MACState(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static MACState getByState(int state) {
        for (MACState macState : values()) {
            if (macState.getType() == state) {
                return macState;
            }
        }
        return OTHER;
    }

    public static String getNameByState(int state) {
        MACState macState = getByState(state);
        if (macState == null) {
            return String.valueOf(state);
        } else {
            return macState.getName();
        }
    }
}
