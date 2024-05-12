package cn.mw.monitor.script.enums;

/**
 * @author gui.quanwang
 * @className ScriptExecStatus
 * @description 脚本执行状态
 * @date 2022/4/15
 */
public enum ScriptExecStatus {
    /**
     * 初始化
     */
    INIT(0, "初始化状态"),

    /**
     * 执行中
     */
    EXECUTING(1, "执行中"),

    /**
     * 结束
     */
    FINISHED(2, "执行结束"),

    /**
     * 失败
     */
    FAIL(9, "执行失败");

    int status;

    String desc;

    ScriptExecStatus(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public int getStatus() {
        return status;
    }

    /**
     * 获取执行状态
     *
     * @param status
     * @return
     */
    public static ScriptExecStatus getStatus(int status) {
        for (ScriptExecStatus scriptExecStatus : values()) {
            if (scriptExecStatus.getStatus() == status) {
                return scriptExecStatus;
            }
        }
        return null;
    }
}
