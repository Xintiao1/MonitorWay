package cn.mw.monitor.api.common;

import java.util.Date;
import java.util.Random;
import java.util.UUID;

/**
 * @author xhy
 * @date 2020/4/10 9:48
 */
//生成32位唯一的uuid
public class UuidUtil {

    public static String getUid() {
        UUID uuid = UUID.randomUUID();
        String uid = uuid.toString().replaceAll("-", "");
//      ////System.out.println(uid);
        long time = new Date().getTime();
//      ////System.out.println(time);
        uid = String.valueOf(time) + uid;
//      ////System.out.println(uid);
        return uid.substring(0, 32);
    }

    public static String get16Uid() {
        UUID uuid = UUID.randomUUID();
        String uid = uuid.toString().replaceAll("-", "");
//      ////System.out.println(uid);
        long time = new Date().getTime();
//      ////System.out.println(time);
        uid = String.valueOf(time) + uid;
//      ////System.out.println(uid);
        return uid.substring(0, 16);
    }

//    public static void main(String[] args) {
//        ////System.out.println(UuidUtil.getUid());
//    }

    public static String nextUuid() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i < 32; i++) {
            int randNum = random.nextInt(9) + 1;
            String num = randNum + "";
            sb = sb.append(num);
        }
        return sb.toString();
    }

}
