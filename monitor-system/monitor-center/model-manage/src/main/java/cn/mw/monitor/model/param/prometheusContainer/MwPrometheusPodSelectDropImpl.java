package cn.mw.monitor.model.param.prometheusContainer;

import cn.mw.monitor.model.service.MwModelInstanceService;
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

import static cn.mw.monitor.model.param.PrometheusContainerEnum.PROMETHEUS_NAMESPACE;
import static cn.mw.monitor.model.param.PrometheusContainerEnum.PROMETHEUS_POD;
import static cn.mw.monitor.service.model.service.MwModelViewCommonService.*;
import static cn.mw.monitor.service.model.util.ValConvertUtil.intValueConvert;

@Service
@Slf4j
public class MwPrometheusPodSelectDropImpl implements MwPrometheusSelectDropChange {
    @Autowired
    private MwModelInstanceService mwModelInstanceService;
    @Autowired
    private MwModelCommonService mwModelCommonService;
    private final static String TYPE = "type";
    @Override
    public String getType() {
        return PROMETHEUS_POD.getType();
    }

    @Override
    public Object getData(Object data) throws Exception {
        List<PrometheusNameSpaceSelectParam> nameSpaceSelectList = new ArrayList<>();
        //获取当前pod的id
        Integer instanceId = intValueConvert(data);
        //获取es中pod的数据
        Map<String, Object> map = mwModelInstanceService.selectInfoByInstanceId(instanceId);
        if (!CollectionUtils.isEmpty(map)) {
            PrometheusPodSelectParam podSelectInfo = JSONObject.parseObject(JSONObject.toJSONString(map), PrometheusPodSelectParam.class);
            //根据pod中nameSpace和monitorServerId查询对应的es中nameSpace数据
            QueryEsParam queryEsParam = new QueryEsParam();
            List<QueryModelInstanceByPropertyIndexParam> paramLists = new ArrayList<>();
            QueryModelInstanceByPropertyIndexParam qParam = new QueryModelInstanceByPropertyIndexParam();
            //设置实例名称  字符串查询设置keyword类型
            qParam.setPropertiesIndexId(INSTANCE_NAME_KEY);
            qParam.setPropertiesType(ModelPropertiesType.STRING.getCode());
            qParam.setPropertiesValueList(Arrays.asList(podSelectInfo.getNamespace()));
            paramLists.add(qParam);
            qParam = new QueryModelInstanceByPropertyIndexParam();
            //设置监控服务器Id
            qParam.setPropertiesIndexId(MONITOR_SERVER_ID);
            qParam.setPropertiesValueList(Arrays.asList(podSelectInfo.getMonitorServerId()));
            paramLists.add(qParam);
            qParam = new QueryModelInstanceByPropertyIndexParam();
            qParam.setPropertiesIndexId(TYPE);
            qParam.setPropertiesType(ModelPropertiesType.STRING.getCode());
            qParam.setPropertiesValueList(Arrays.asList(PROMETHEUS_NAMESPACE.getType()));
            paramLists.add(qParam);
            queryEsParam.setParamLists(paramLists);
            //获取es中的nameSpace数据
            List<Map<String, Object>> nameSpaceList = mwModelCommonService.getAllInstanceInfoByQueryParam(queryEsParam);
            if (CollectionUtils.isNotEmpty(nameSpaceList)) {
                Map<String, Object> nameSpaceMap = nameSpaceList.get(0);
                PrometheusNameSpaceSelectParam nameSpaceInfo = JSONObject.parseObject(JSONObject.toJSONString(nameSpaceMap), PrometheusNameSpaceSelectParam.class);
                if(nameSpaceInfo!=null){
                    nameSpaceInfo.setPodList(Arrays.asList(podSelectInfo));
                }else{
                    nameSpaceInfo.setPodList(new ArrayList<>());
                }
                nameSpaceSelectList.add(nameSpaceInfo);
            }
        }
        return nameSpaceSelectList;

    }
}
