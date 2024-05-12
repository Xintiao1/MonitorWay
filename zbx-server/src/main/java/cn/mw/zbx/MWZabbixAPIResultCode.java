package cn.mw.zbx;

public enum MWZabbixAPIResultCode {

    /* 成功状态码 */
    SUCCESS(0, "Success"),
    /* 参数错误：10001-19999 */
    PARAM_IS_INVALID(10001, "Param is invalid.");

    private Integer code;

    private String message;

    MWZabbixAPIResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer code() {
        return this.code;
    }

    public String message() {
        return this.message;
    }

}
