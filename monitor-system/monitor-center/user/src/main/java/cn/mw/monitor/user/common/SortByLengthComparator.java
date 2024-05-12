package cn.mw.monitor.user.common;

import java.util.Comparator;

/**
 * Created by zy.quaee on 2021/5/9 22:33.
 **/
public class SortByLengthComparator implements Comparator<AdDepartment> {

    @Override
    public int compare(AdDepartment o1, AdDepartment o2) {
        if (o1.getcName().length() > o2.getcName().length() | o1.getcName().length() == o2.getcName().length()) {
            return 1;
        }else {
            return -1;
        }
    }
}
