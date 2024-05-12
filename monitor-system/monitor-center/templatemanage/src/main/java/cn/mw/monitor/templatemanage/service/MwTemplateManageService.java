package cn.mw.monitor.templatemanage.service;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.templatemanage.entity.AddTemplateManageParam;
import cn.mw.monitor.templatemanage.entity.ParamEntity;
import cn.mw.monitor.templatemanage.entity.QueryTemplateManageParam;

import java.util.List;

public interface MwTemplateManageService {

    //查询规格型号
    Reply specification(ParamEntity specification) throws Exception;

    //查询品牌
    Reply brand(ParamEntity brand) throws Exception;

    Reply selectList1(QueryTemplateManageParam param);

    Reply selectList(QueryTemplateManageParam param);

    //删除模板管理
    Reply delete(List<Integer> record) throws Exception;

    //修改模板管理
    Reply update(AddTemplateManageParam record) throws Exception;

    //添加模板管理
    Reply insert(AddTemplateManageParam record) throws Exception;

    Reply selectListDropDown();
}
