package cn.mw.monitor.activiti.param;

public enum ArtificialCheckTypeEnum {
    Unknown(-1),Role(1),Group(2), People(3);

    private int code;

    ArtificialCheckTypeEnum(int code){
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ArtificialCheckTypeEnum valueOf(int code) {
        ArtificialCheckTypeEnum[] values = ArtificialCheckTypeEnum.values();

        for (ArtificialCheckTypeEnum value : values) {
            if (value.getCode() == code) {
                return value;
            }
        }
        return Unknown;
    }
}
