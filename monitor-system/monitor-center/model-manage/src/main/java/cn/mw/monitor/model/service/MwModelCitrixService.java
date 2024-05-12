package cn.mw.monitor.model.service;

import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.model.param.citrix.MwAdvancedQueryCitrixParam;
import cn.mw.monitor.model.param.citrix.MwQueryCitrixParam;
import cn.mwpaas.common.model.Reply;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author qzg
 * @date 2022/10/8
 */
public interface MwModelCitrixService {
    Reply loginClientGetData(MwQueryCitrixParam param);

    Reply getCitrixTreeInfo();

    Reply getLBCitrixRelationList(MwQueryCitrixParam param);

    Reply getGSLBCitrixRelationList(MwQueryCitrixParam param);

    Reply exportCitrixRelationList(MwQueryCitrixParam param, HttpServletRequest request, HttpServletResponse response);

    TimeTaskRresult getCitrixInfoByTaskTime();

    Reply getAllModelCitrixAssets();

    Reply advancedQueryCitrixInfo(MwAdvancedQueryCitrixParam param);
}
