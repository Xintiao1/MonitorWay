package cn.mwpaas.common.utils;

import java.util.UUID;

/**
 * @author phzhou
 * @ClassName UUIDUtils
 * @CreateDate 2019/2/22
 * @Description
 */
public class UUIDUtils {

    public static String getUUID(){
        return UUID.randomUUID().toString().replace("-", "");
    }

}
