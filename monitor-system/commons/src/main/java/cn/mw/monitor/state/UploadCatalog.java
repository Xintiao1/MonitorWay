package cn.mw.monitor.state;

public enum UploadCatalog {
    UPLOAD(1,"用户文件上传目录"),
    BASIC(0,"basic目录");
    private int code;
    private String name;

    UploadCatalog(int code, String name) {
        this.code=code;
        this.name=name;
    }
    public  Integer getCode(){
        return code;
    }
    public String getName(){
        return name;
    }

    public static UploadCatalog getByValue(int value){
        for (UploadCatalog code:UploadCatalog.values()) {
            if(code.getCode()==value){
                return code;
            }
        }
        return null;
    }


}
