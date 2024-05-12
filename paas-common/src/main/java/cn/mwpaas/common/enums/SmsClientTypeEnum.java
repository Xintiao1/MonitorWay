package cn.mwpaas.common.enums;

/**
 * @author phzhou
 * @ClassName SmsClientTypeEnum
 * @CreateDate 2019/3/5
 * @Description
 */
public enum SmsClientTypeEnum {
    IYIHU(1, "iyihu"),
    RUANWEI(2, "ruanwei");
    private Integer code;
    private String name;

    SmsClientTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
