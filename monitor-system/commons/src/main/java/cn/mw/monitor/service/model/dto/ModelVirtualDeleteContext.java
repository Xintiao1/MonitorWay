package cn.mw.monitor.service.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class ModelVirtualDeleteContext {
    //VCenter关联的实例id
    private List<Integer> instanceIds;
}
