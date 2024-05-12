package cn.mw.monitor.screen.param;

import cn.mw.monitor.screen.model.IndexBulk;
import cn.mw.monitor.screen.model.IndexModelBase;
import lombok.Data;

import java.util.List;
@Data
public class EditorIndexParam {
    //编辑后模块list
//    private List<IndexModelBase> componentList;
    private List<IndexBulk> componentList;
    //模块id
    private Integer bulkId;
    private String bulkName;
    //首页模块编辑类型（0-移动，1-添加，2-删除）
    private Integer performType;

    private String modelDataId;
}
