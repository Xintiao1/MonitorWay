package cn.mwpaas.common.utils;

/**
 * @author phzhou
 * @ClassName BooleanUtils
 * @CreateDate 2019/4/8
 * @Description
 */
public class BooleanUtils {

    public static boolean isTrue(Boolean flag) {
        return flag == null ? false : flag;
    }

    public static boolean isFalse(Boolean flag) {
        return flag == null || !flag;
    }

//    public static void main(String[] args) {
//        ////System.out.println(BooleanUtils.isTrue(true));
//    }
}

