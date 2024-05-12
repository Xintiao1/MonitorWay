package cn.mw.monitor.api.dataview;

public enum AssetAddType {
    EXSIST(1,"EXIST", "已添加"), NOTEXSIST(0,"NOTEXIST", "未添加");

    private int code;
    private String name;
    private String chnName;

    AssetAddType(int code, String name, String chnName){
        this.code = code;
        this.name = name;
        this.chnName = chnName;
    }

    public String getChnName() {
        return chnName;
    }
}
