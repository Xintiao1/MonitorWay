package cn.mw.monitor.model.type;

/**
 * @author guiquanwnag
 * @datetime 2023/7/2
 * @Description interface state
 */
public enum InterfaceState {

    UP(1, "up"),
    DOWN(2, "down"),
    TESTING(3, "testing"),
    UNKNOWN(4, "unknown"),
    DORMANT(5, "dormant"),
    NOTPRESENT(6, "notPresent"),
    LOWERLAYERDOWN(7, "lowerLayerDown");

    private int state;

    private String name;

    InterfaceState(int state, String name) {
        this.state = state;
        this.name = name;
    }

    public int getState() {
        return state;
    }

    public String getName() {
        return name;
    }

    public static InterfaceState getByState(int state) {
        for (InterfaceState interfaceState : values()) {
            if (interfaceState.getState() == state) {
                return interfaceState;
            }
        }
        return UNKNOWN;
    }

    public static String getNameByState(int state) {
        InterfaceState interfaceState = getByState(state);
        if (interfaceState == null) {
            return String.valueOf(state);
        } else {
            return interfaceState.getName();
        }
    }

}
