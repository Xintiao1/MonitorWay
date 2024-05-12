package cn.mw.monitor.service.zbx.model;

public enum HostProblemType {
    notclassified(0 ,false)
    ,information(1 ,false)
    ,warning(2 ,true)
    ,average(3,true)
    ,high(4,true)
    ,disaster(5,true);

    private int code;
    private boolean hasProblem;

    HostProblemType(int code ,boolean hasProblem){
        this.code = code;
        this.hasProblem = hasProblem;
    }

    public int getCode() {
        return code;
    }

    public boolean isHasProblem() {
        return hasProblem;
    }

    public static HostProblemType getType(String code){
        int value = Integer.parseInt(code);
        for(HostProblemType type : HostProblemType.values()){
            if(type.getCode() == value){
                return type;
            }
        }
        return null;
    }
}
