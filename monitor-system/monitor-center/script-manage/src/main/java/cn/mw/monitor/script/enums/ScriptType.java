package cn.mw.monitor.script.enums;

/**
 * @author gui.quanwang
 * @className ScriptType
 * @description 脚本类型枚举
 * @date 2022/4/12
 */
public enum ScriptType {
    /**
     * linux的shell
     */
    SHELL(0, "sh", "shell", 1),
    /**
     * windows的cmd
     */
    CMD(1, "batchfile", "cmd", 2),
    /**
     * perl
     */
    PERL(3, "perl", "perl", 0),
    /**
     * python
     */
    PYTHON(2, "python", "python", 0),
    /**
     * powershell
     */
    POWERSHELL(4, "powershell", "powershell", 0),
    DEVICE(6, "device", "shell", 1),
    /**
     * sql
     */
    SQL(5, "sql", "sql", 3);



    /**
     * 脚本类别（1：shell  2：cmd）
     */
    private Integer typeId;

    /**
     * 脚本类型名称
     */
    private String typeName;

    /**
     * 脚本类型描述
     */
    private String typeDesc;

    /**
     * 账户类别ID
     */
    private Integer accountTypeId;

    public Integer getTypeId() {
        return typeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public Integer getAccountTypeId() {
        return accountTypeId;
    }

    ScriptType(Integer typeId, String typeName, String typeDesc, Integer accountTypeId) {
        this.typeId = typeId;
        this.typeName = typeName;
        this.typeDesc = typeDesc;
        this.accountTypeId = accountTypeId;
    }

    public static ScriptType getScriptType(String typeName) {
        for (ScriptType scriptType : values()) {
            if (scriptType.getTypeName().equals(typeName)) {
                return scriptType;
            }
        }
        return null;
    }


    public static ScriptType getScriptType(int typeId) {
        for (ScriptType scriptType : values()) {
            if (scriptType.getTypeId().equals(typeId)) {
                return scriptType;
            }
        }
        return null;
    }
}
