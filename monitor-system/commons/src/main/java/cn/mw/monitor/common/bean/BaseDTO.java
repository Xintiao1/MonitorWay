package cn.mw.monitor.common.bean;

import java.io.Serializable;

/**
 * Created by yeshengqi on 2019/4/25.
 */

public class BaseDTO implements Serializable {

    private static final long serialVersionUID = -2004362017453662885L;

    /**
     * 第几页
     */
    private int pageNumber;

    /**
     * 每页显示行数
     */
    private int pageSize;

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
