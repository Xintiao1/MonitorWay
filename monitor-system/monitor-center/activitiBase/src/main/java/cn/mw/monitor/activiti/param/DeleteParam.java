package cn.mw.monitor.activiti.param;

import lombok.Data;

import java.util.List;

@Data
public class DeleteParam {
    private List<String> processIds;
}
