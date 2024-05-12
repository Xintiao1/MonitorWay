package cn.mw.monitor.service.assets.param;

public abstract class MaintainTransform {
    public static final int[] dayOfWeeks = {1, 2, 4, 8, 16, 32, 64};
    public static final int[] months = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048};

    abstract void tranform(MwAssetsMainTainZabbixParam param);

    abstract void setDurationDay(Integer durationDay);

    abstract void setDurationHour(Integer durationHour);

    abstract void setDurationMin(Integer durationMin);

    abstract void setStartHour(Integer startHour);

    abstract void setStartMin(Integer startMin);

    protected void setCommon(MwAssetsMainTainZabbixParam param){
        Integer period = param.getPeriod();
        int durationDay = period / (24 * 3600);

        int durationHour = 0;
        if(durationDay > 0){
            period = period - durationDay * 24 * 3600;
            durationHour = period / 3600;
        }

        int durationMin = 0;
        if(durationHour > 0){
            period = period - durationHour * 3600;
            durationMin = period / 60;
        }

        setDurationDay(durationDay);
        setDurationHour(durationHour);
        setDurationMin(durationMin);

        int startTime = param.getStart_time();
        int startHour = startTime / 3600;
        if(startHour > 0){
            startTime = startTime - startHour * 3600;
        }
        int startMin = startTime / 60;

        setStartHour(startHour);
        setStartMin(startMin);
    }

    public static byte[] intToBytes(int value) {
        byte[] src = new byte[2];
        src[0] = (byte) (value & 0xFF);
        src[1] = (byte) ((value >> 8) & 0xFF);
        return src;
    }

    public static int bytesToInt(byte[] src) {
        int value;
        value = (int) ((src[0] & 0xFF)
                | ((src[1] & 0xFF)<<8));
        return value;
    }
}
