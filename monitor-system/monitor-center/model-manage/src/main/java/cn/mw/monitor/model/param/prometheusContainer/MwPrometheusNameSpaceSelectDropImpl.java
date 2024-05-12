package cn.mw.monitor.model.param.prometheusContainer;

import cn.mw.monitor.model.dao.MwModelInstanceDao;
import cn.mw.monitor.model.dao.MwModelManageDao;
import cn.mw.monitor.model.service.MwModelInstanceService;
import cn.mw.monitor.model.service.MwModelViewService;
import cn.mw.monitor.service.model.param.MwModelInstanceCommonParam;
import cn.mw.monitor.service.model.param.QueryRelationInstanceModelParam;
import cn.mw.monitor.service.model.util.MwModelUtils;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.mw.monitor.model.param.PrometheusContainerEnum.PROMETHEUS_NAMESPACE;
import static cn.mw.monitor.model.param.PrometheusContainerEnum.PROMETHEUS_POD;
import static cn.mw.monitor.service.model.util.ValConvertUtil.intValueConvert;

@Service
@Slf4j
public class MwPrometheusNameSpaceSelectDropImpl implements MwPrometheusSelectDropChange {
    @Autowired
    private MwModelInstanceService mwModelInstanceService;
    @Autowired
    private MwModelManageDao mwModelManageDao;
    @Resource
    private MwModelInstanceDao mwModelInstanceDao;
    @Autowired
    private MwModelViewService mwModelViewService;
    private final static String TYPE = "type";

    @Override
    public String getType() {
        return PROMETHEUS_NAMESPACE.getType();
    }

    @Override
    public Object getData(Object data) throws Exception {
        List<PrometheusNameSpaceSelectParam> nameSpaceSelectList = new ArrayList<>();
        //nameSpace实例id
        Integer instanceId = intValueConvert(data);
        //获取ES中nameSpace数据
        Map<String, Object> map = mwModelInstanceService.selectInfoByInstanceId(instanceId);
        if (!CollectionUtils.isEmpty(map)) {
            PrometheusNameSpaceSelectParam nameSpaceSelectInfo = JSONObject.parseObject(JSONObject.toJSONString(map), PrometheusNameSpaceSelectParam.class);
            //根据PODe的ModelId获取所有的实例instanceId
            List<MwModelInstanceCommonParam> podInstanceInfo = mwModelInstanceDao.selectModelInstanceInfoById(PROMETHEUS_POD.getModelId());
            Set<Integer> podInstanceIds = podInstanceInfo.stream().map(s -> s.getModelInstanceId()).collect(Collectors.toSet());
            List<String> modelIndexs = mwModelManageDao.selectModelIndexsByModelIds(Arrays.asList(PROMETHEUS_POD.getModelId()));
            QueryRelationInstanceModelParam param1 = new QueryRelationInstanceModelParam();
            param1.setModelIndexs(modelIndexs);
            param1.setInstanceIds(new ArrayList<>(podInstanceIds));
            //根据instanceId获取所有的Pod数据
            List<Map<String, Object>> listMap = mwModelViewService.selectInstanceInfoByIdsAndModelIndexs(param1);
            List<PrometheusPodSelectParam> podSelectList = MwModelUtils.convertEsData(PrometheusPodSelectParam.class, listMap);
            //Pod数据转为nameSpace为key的map
            Map<String, List<PrometheusPodSelectParam>> podGroupByNameSpaceMap = podSelectList.stream().filter(s -> !Strings.isNullOrEmpty(s.getNamespace())).collect(Collectors.groupingBy(s -> s.getNamespace()));
            String namespace = nameSpaceSelectInfo.getNamespace();
            if (!CollectionUtils.isEmpty(podGroupByNameSpaceMap) && podGroupByNameSpaceMap.containsKey(namespace)) {
                List<PrometheusPodSelectParam> prometheusPodSelectList = podGroupByNameSpaceMap.get(namespace);
                if(CollectionUtils.isNotEmpty(prometheusPodSelectList)){
                    nameSpaceSelectInfo.setPodList(prometheusPodSelectList);
                }
            }else{
                nameSpaceSelectInfo.setPodList(new ArrayList<>());
            }
            nameSpaceSelectList.add(nameSpaceSelectInfo);
        }
        return nameSpaceSelectList;
    }
}
