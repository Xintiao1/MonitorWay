package cn.mw.monitor.logManage.dto;

import lombok.Data;

import java.util.List;

@Data
public class DataConversion {

    private String modelName;

    private List<Content> contentList;

}
