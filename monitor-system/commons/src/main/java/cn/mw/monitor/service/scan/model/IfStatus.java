package cn.mw.monitor.service.scan.model;

public enum IfStatus {
    up(1), down(2), testing(3), unknown(4)
    ,dormant(5),notPresent(6),lowerLayerDown(7);

    private int code;

    public int getCode() {
        return code;
    }

    IfStatus(int code ){
        this.code = code;
    }

    public static IfStatus getIfStatus(int code){
        IfStatus[]  values = IfStatus.values();
        for(IfStatus ifStatus : values){
            if(code == ifStatus.getCode()){
                return ifStatus;
            }
        }

        return unknown;
    }
}
