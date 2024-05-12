package cn.mw.monitor.assetsTemplate.service;


import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.assetsTemplate.api.param.assetsTemplate.AddAssetsTemplateParam;
import cn.mw.monitor.assetsTemplate.api.param.assetsTemplate.QueryAssetsTemplateParam;

import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/20
 */
public interface MwAssetsTemplateService {
    Reply selectById(Integer id);

    /*
     * 查询模版信息并且关联mapper表
     */
    Reply selectList(QueryAssetsTemplateParam qsDTO);

    /*
     * 查询模版信息
     */
    Reply selectTepmplateTableList(QueryAssetsTemplateParam qsParam);

    Reply update(AddAssetsTemplateParam ausDTO) throws Exception;

    Reply delete(List<Integer> ids);

    Reply insert(AddAssetsTemplateParam ausDTO) throws Exception;

    Reply templateGet(String name);

//    Reply resetTemplateIdBatch();

    Reply updateAssetsTemplate();

    Reply fuzzSearchAllFiledData(String value);
}
