package cn.mw.monitor.service.model.param;

import cn.mw.monitor.bean.BaseParam;
import lombok.Data;

import java.util.List;

/**
 * @author qzg
 * @date 2022/3/07 14:44
 */
@Data
public class QueryEsParam extends BaseParam {
    private List<String> modelIndexs;
    private List<QueryModelInstanceByPropertyIndexParam> paramLists;
    //必须存在的字段
    private List<String> existsList;
}
