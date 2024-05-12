package cn.mw.monitor.model.param.prometheusContainer;

import cn.mw.monitor.model.dao.MwModelManageDao;
import cn.mw.monitor.model.service.MwModelInstanceService;
import cn.mw.monitor.model.service.MwModelViewService;
import cn.mw.monitor.service.model.param.QueryEsParam;
import cn.mw.monitor.service.model.param.QueryModelInstanceByPropertyIndexParam;
import cn.mw.monitor.service.model.service.ModelPropertiesType;
import cn.mw.monitor.service.model.service.MwModelCommonService;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static cn.mw.monitor.model.param.PrometheusContainerEnum.*;
import static cn.mw.monitor.service.model.service.MwModelViewCommonService.INSTANCE_NAME_KEY;
import static cn.mw.monitor.service.model.service.MwModelViewCommonService.MONITOR_SERVER_ID;
import static cn.mw.monitor.service.model.util.ValConvertUtil.intValueConvert;

@Service
@Slf4j
public class MwPrometheusContainerSelectDropImpl implements MwPrometheusSelectDropChange {
    @Autowired
    private MwModelInstanceService mwModelInstanceService;
    @Autowired
    private MwModelManageDao mwModelManageDao;
    @Autowired
    private MwModelCommonService mwModelCommonService;
    private final static String TYPE = "type";

    static final String NAME_SPACE = "namespace";

    @Override
    public String getType() {
        return PROMETHEUS_CONTAINER.getType();
    }

    @Override
    public Object getData(Object data) throws Exception {
        List<PrometheusNameSpaceSelectParam> nameSpaceSelectList = new ArrayList<>();
        //获取当前Container的id
        Integer instanceId = intValueConvert(data);
        //获取es中Container的数据
        Map<String, Object> containerMap = mwModelInstanceService.selectInfoByInstanceId(instanceId);
        if (!CollectionUtils.isEmpty(containerMap)) {
            PrometheusPodSelectParam containerSelectInfo = JSONObject.parseObject(JSONObject.toJSONString(containerMap), PrometheusPodSelectParam.class);
            //根据Container中nameSpace和monitorServerId查询对应的es中nameSpace数据
            QueryEsParam queryEsParam = new QueryEsParam();
            List<QueryModelInstanceByPropertyIndexParam> paramLists = new ArrayList<>();
            QueryModelInstanceByPropertyIndexParam qParam = new QueryModelInstanceByPropertyIndexParam();
            qParam.setPropertiesIndexId(INSTANCE_NAME_KEY);
            qParam.setPropertiesType(ModelPropertiesType.STRING.getCode());
            qParam.setPropertiesValueList(Arrays.asList(containerSelectInfo.getNamespace()));
            paramLists.add(qParam);
            qParam = new QueryModelInstanceByPropertyIndexParam();
            qParam.setPropertiesIndexId(MONITOR_SERVER_ID);
            qParam.setPropertiesValueList(Arrays.asList(containerSelectInfo.getMonitorServerId()));
            paramLists.add(qParam);
            qParam = new QueryModelInstanceByPropertyIndexParam();
            qParam.setPropertiesIndexId(TYPE);
            qParam.setPropertiesType(ModelPropertiesType.STRING.getCode());
            qParam.setPropertiesValueList(Arrays.asList(PROMETHEUS_NAMESPACE.getType()));
            paramLists.add(qParam);
            queryEsParam.setParamLists(paramLists);
            List<Map<String, Object>> nameSpaceList = mwModelCommonService.getAllInstanceInfoByQueryParam(queryEsParam);
            if (CollectionUtils.isNotEmpty(nameSpaceList)) {
                PrometheusNameSpaceSelectParam nameSpaceSelectInfo = JSONObject.parseObject(JSONObject.toJSONString(nameSpaceList.get(0)), PrometheusNameSpaceSelectParam.class);
                //根据Container中nameSpace,pod和monitorServerId查询对应的es中pod数据
                queryEsParam = new QueryEsParam();
                paramLists = new ArrayList<>();
                qParam = new QueryModelInstanceByPropertyIndexParam();
                qParam.setPropertiesIndexId(INSTANCE_NAME_KEY);
                qParam.setPropertiesType(ModelPropertiesType.STRING.getCode());
                qParam.setPropertiesValueList(Arrays.asList(containerSelectInfo.getPod()));
                paramLists.add(qParam);
                qParam = new QueryModelInstanceByPropertyIndexParam();
                qParam.setPropertiesIndexId(NAME_SPACE);
                qParam.setPropertiesType(ModelPropertiesType.STRING.getCode());
                qParam.setPropertiesValueList(Arrays.asList(containerSelectInfo.getNamespace()));
                paramLists.add(qParam);
                qParam = new QueryModelInstanceByPropertyIndexParam();
                qParam.setPropertiesIndexId(TYPE);
                qParam.setPropertiesType(ModelPropertiesType.STRING.getCode());
                qParam.setPropertiesValueList(Arrays.asList(PROMETHEUS_POD.getType()));
                paramLists.add(qParam);
                qParam = new QueryModelInstanceByPropertyIndexParam();
                qParam.setPropertiesIndexId(MONITOR_SERVER_ID);
                qParam.setPropertiesValueList(Arrays.asList(containerSelectInfo.getMonitorServerId()));
                paramLists.add(qParam);
                queryEsParam.setParamLists(paramLists);
                List<Map<String, Object>> podList = mwModelCommonService.getAllInstanceInfoByQueryParam(queryEsParam);
                if (CollectionUtils.isNotEmpty(podList)) {
                    PrometheusPodSelectParam podSelectInfo = JSONObject.parseObject(JSONObject.toJSONString(podList.get(0)), PrometheusPodSelectParam.class);
                    if(podSelectInfo!=null){
                        nameSpaceSelectInfo.setPodList(Arrays.asList(podSelectInfo));
                    }else{
                        nameSpaceSelectInfo.setPodList(new ArrayList<>());
                    }
                }
                nameSpaceSelectList.add(nameSpaceSelectInfo);
            }
        }
        return nameSpaceSelectList;

    }
}
