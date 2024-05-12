package cn.mw.monitor.util;

import java.util.ArrayList;
import java.util.List;

/**
 * 此工具类为了防止SQL写入数据库中，一次性写入数量过多，导致数据库宕机。
 * 因为mysql默认一次性最大写入数据量为1000，更多的数据写入时不再被插入，
 * 可能会导致数据丢失等情况。
 * 此工具类会对要插入数据库的List集合进行检索，若
 */
public class InsertMaxNumUtil {

    private final static Integer INSERT_MAX_NUM = 500;

    public static List<List<?>> checkList(List<?> list) {
        int size = list.size();
        if (list == null || size == 0) {
            return null;
        } else {
            List<List<?>> listList = new ArrayList<List<?>>();
            if (size > INSERT_MAX_NUM) {
                int quo = size / INSERT_MAX_NUM;
                int rem = size % INSERT_MAX_NUM;
                if (rem > 0 ) {
                    quo = quo + 1;
                }
                for (int i = 0; i < quo; i++) {
                    List<?> subList = list.subList(i * INSERT_MAX_NUM,
                            (i + 1) * INSERT_MAX_NUM > size ? size : (i + 1) * INSERT_MAX_NUM);
                    listList.add(subList);
                }
            } else {
                listList.add(list);
            }
            return listList;
        }
    }

    /**
     * 这个工具类不必要一定使用，可以对insert的SQL数据量进行评估
     * 感觉超出可能超出1000条，建议使用这个工具类
     */
//    public static void main(String[] args) {
//        // 假设List<Integer>的Integer是要insert到数据路的类
//        List<Integer> list = new ArrayList<>();
//        // 添加9500条数据
//        for (int i = 0; i < 9500; i++) {
//            list.add(i);
//        }
//        // 一般直接插入list作为参数去插入数据库，数据库会报错
//        // xxDao.insert(list); =====>>>>>> 会抛出异常
//        // 使用这个工具类对list进行校验和拆解
//        List<List<?>> listList = InsertMaxNumUtil.checkList(list);
//        // 这里对拆解得到的List集合进行遍历
//        for (List ll : listList) {
//            // 这里在进行insert操作，就不用担心，数据大于1000
//            // xxDao.insert(ll); ====>>>> 经过校验，不用担心异常
//            ////System.out.println(ll);
//        }
//
//    }

}
