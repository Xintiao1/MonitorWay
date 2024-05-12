package cn.mw.monitor.logManage.param;

import lombok.Data;

import java.util.List;

@Data
public class ParsingLibDeleteParam {

    private List<Integer> ids;

    /**
     * true，强制删除 false 先检查后根据选择是否强制删除
     */
    private boolean flag;
}
