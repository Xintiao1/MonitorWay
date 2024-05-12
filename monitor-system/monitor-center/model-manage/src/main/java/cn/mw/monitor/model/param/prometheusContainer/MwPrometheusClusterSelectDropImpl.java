package cn.mw.monitor.model.param.prometheusContainer;

import cn.mw.monitor.model.dao.MwModelManageDao;
import cn.mw.monitor.model.service.MwModelViewService;
import cn.mw.monitor.service.model.param.QueryRelationInstanceModelParam;
import cn.mw.monitor.service.model.util.MwModelUtils;
import cn.mwpaas.common.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cn.mw.monitor.model.param.PrometheusContainerEnum.*;
import static cn.mw.monitor.service.model.util.ValConvertUtil.intValueConvert;

@Service
@Slf4j
public class MwPrometheusClusterSelectDropImpl implements MwPrometheusSelectDropChange {
    @Autowired
    private MwModelManageDao mwModelManageDao;
    @Autowired
    private MwModelViewService mwModelViewService;
    private final static String TYPE = "type";

    @Override
    public String getType() {
        return PROMETHEUS_CLUSTER.getType();
    }

    @Override
    public Object getData(Object data) throws Exception {
        List<PrometheusNameSpaceSelectParam> nameSpaceSelectList = new ArrayList<>();
        //cluster实例Id
        Integer instanceId = intValueConvert(data);
        //cluster级别 ，获取所有nameSpace、pod
        List<String> modelIndexs = mwModelManageDao.selectModelIndexsByModelIds(Arrays.asList(PROMETHEUS_NAMESPACE.getModelId(), PROMETHEUS_POD.getModelId()));
        QueryRelationInstanceModelParam param1 = new QueryRelationInstanceModelParam();
        //根据relationInstanceId获取所有的监控容器数据
        param1.setRelationInstanceIds(Arrays.asList(instanceId));
        param1.setModelIndexs(modelIndexs);
        List<Map<String, Object>> listMap = mwModelViewService.selectInstanceInfoByRelationInstanceIdList(param1);
        //获取所有nameSpace数据
        List<Map<String, Object>> nameSpaceMap = listMap.stream().filter(s -> PROMETHEUS_NAMESPACE.getType().equals(s.get(TYPE))).collect(Collectors.toList());
        nameSpaceSelectList = MwModelUtils.convertEsData(PrometheusNameSpaceSelectParam.class, nameSpaceMap);

        //获取所有pod数据
        List<Map<String, Object>> podMap = listMap.stream().filter(s -> PROMETHEUS_POD.getType().equals(s.get(TYPE))).collect(Collectors.toList());
        List<PrometheusPodSelectParam> podSelectList = MwModelUtils.convertEsData(PrometheusPodSelectParam.class, podMap);
        Map<String, List<PrometheusPodSelectParam>> podGroupByNameSpaceMap = podSelectList.stream().filter(s -> !Strings.isNullOrEmpty(s.getNamespace())).collect(Collectors.groupingBy(s -> s.getNamespace()));
        //循环所有nameSpace，获取对应的pod数据
        for (PrometheusNameSpaceSelectParam nameSpaceSelectParam : nameSpaceSelectList) {
            String namespace = nameSpaceSelectParam.getNamespace();
            if (!CollectionUtils.isEmpty(podGroupByNameSpaceMap) && podGroupByNameSpaceMap.containsKey(namespace)) {
                List<PrometheusPodSelectParam> prometheusPodSelectList = podGroupByNameSpaceMap.get(namespace);
                if (CollectionUtils.isNotEmpty(prometheusPodSelectList)) {
                    nameSpaceSelectParam.setPodList(prometheusPodSelectList);
                }
            } else {
                nameSpaceSelectParam.setPodList(new ArrayList<>());
            }
        }
        return nameSpaceSelectList;
    }
}
