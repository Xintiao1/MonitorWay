package cn.mw.monitor.model.param;

import lombok.Data;

import java.util.List;

@Data
public class BatchUpdateInstanceParam {
    private List<Integer> ids;
    private String notifyInfo;
}
