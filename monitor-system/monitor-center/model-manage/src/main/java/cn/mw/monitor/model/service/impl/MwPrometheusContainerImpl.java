package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.model.dao.MwModelInstanceDao;
import cn.mw.monitor.model.dao.MwModelManageDao;
import cn.mw.monitor.model.exception.SyncConnectException;
import cn.mw.monitor.model.param.AddAndUpdateModelInstanceParam;
import cn.mw.monitor.model.param.DeleteModelInstanceParam;
import cn.mw.monitor.model.param.InstanceSyncParam;
import cn.mw.monitor.model.param.PrometheusContainerEnum;
import cn.mw.monitor.model.param.prometheusContainer.MwPrometheusSelectDropChangeManage;
import cn.mw.monitor.model.param.prometheusContainer.PrometheusInstanceParam;
import cn.mw.monitor.model.param.prometheusContainer.PrometheusNameSpaceSelectParam;
import cn.mw.monitor.model.service.MwModelInstanceService;
import cn.mw.monitor.model.service.MwModelViewService;
import cn.mw.monitor.model.service.MwPrometheusContainerService;
import cn.mw.monitor.prometheus.constants.QueryConstants;
import cn.mw.monitor.prometheus.service.impl.PrometheusApiConnectorImpl;
import cn.mw.monitor.prometheus.utils.PrometheusApiConnectorFactory;
import cn.mw.monitor.prometheus.vo.PanelQueryParamVo;
import cn.mw.monitor.prometheus.vo.PrometheusResponseVo;
import cn.mw.monitor.prometheus.vo.PrometheusResultVo;
import cn.mw.monitor.service.model.param.QueryRelationInstanceModelParam;
import cn.mw.monitor.service.model.util.MwModelUtils;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.mw.monitor.model.param.PrometheusContainerEnum.*;
import static cn.mw.monitor.service.model.util.ValConvertUtil.intValueConvert;

/**
 * 普罗米修斯容器监控页面
 */
@Service
@Slf4j
public class MwPrometheusContainerImpl implements MwPrometheusContainerService {

    private final static String SUCCESS = "success";
    private int pageSize = 10000;
    private final static String TYPE = "type";
    private final static String NAMESPACE = "namespace";
    @Autowired
    private MwModelInstanceService mwModelInstanceService;
    @Autowired
    private MwModelViewService mwModelViewService;
    @Resource
    private MwModelManageDao mwModelManageDao;
    @Resource
    private MwModelInstanceDao mwModelInstanceDao;

    @Autowired
    private MwPrometheusSelectDropChangeManage selectDropChangeManage;

    @Override
    public Reply syncContainerDeviceInfo(InstanceSyncParam param) throws Exception {
        List<String> modelIndexs = mwModelManageDao.selectModelIndexsByModelIds(PrometheusContainerEnum.getModelIdList());
        QueryRelationInstanceModelParam param1 = new QueryRelationInstanceModelParam();
        param1.setRelationInstanceIds(Arrays.asList(param.getInstanceId()));
        param1.setModelIndexs(modelIndexs);
        param1.setPageSize(pageSize);
        //根据relationInstanceId获取所有的监控容器数据
        List<Map<String, Object>> listMap = mwModelViewService.selectInstanceInfoByRelationInstanceIdList(param1);

        List<PrometheusInstanceParam> prometheusInstanceList = MwModelUtils.convertEsData(PrometheusInstanceParam.class, listMap);


        List<AddAndUpdateModelInstanceParam> allInsertInstanceInfo = new ArrayList<>();
        List<PrometheusInstanceParam> allDeleteInstanceInfo = new ArrayList<>();
        //获取所有nameSpace数据
        getSyncData(param.getServerId(), param.getInstanceId(), prometheusInstanceList, QueryConstants.Query.QUERY_NAMESPACE, PROMETHEUS_NAMESPACE, allInsertInstanceInfo, allDeleteInstanceInfo);

        //获取所有Pod数据
        getSyncData(param.getServerId(), param.getInstanceId(), prometheusInstanceList, QueryConstants.Query.QUERY_POD, PROMETHEUS_POD, allInsertInstanceInfo, allDeleteInstanceInfo);

        //获取所有container数据
        getSyncData(param.getServerId(), param.getInstanceId(), prometheusInstanceList, QueryConstants.Query.QUERY_POD_CONTAINER, PROMETHEUS_CONTAINER, allInsertInstanceInfo, allDeleteInstanceInfo);

        Set<String> modelIndexSet = new HashSet<>();
        Set<Integer> instanceIdSet = new HashSet<>();

        for (PrometheusInstanceParam delInstanceParam : allDeleteInstanceInfo) {
            delInstanceParam.getEsId();
            modelIndexSet.add(delInstanceParam.getModelIndex());
            instanceIdSet.add(delInstanceParam.getModelInstanceId());
        }
        if (CollectionUtils.isNotEmpty(allDeleteInstanceInfo)) {
            //批量删除
            DeleteModelInstanceParam deleteParam1 = new DeleteModelInstanceParam();
            deleteParam1.setModelIndexs(new ArrayList<>(modelIndexSet));
            deleteParam1.setInstanceIds(new ArrayList<>(instanceIdSet));
            deleteParam1.setRelationInstanceIds(Arrays.asList(param.getInstanceId()));
            mwModelInstanceService.batchDeleteInstanceInfo(deleteParam1);
        }

        //批量新增
        mwModelInstanceService.saveData(allInsertInstanceInfo, true, true);

        return Reply.ok();
    }

    @Override
    public Reply  getSelectDropPrometheus(Integer instanceId) throws Exception {
        Integer modelId = mwModelInstanceDao.getModelIdByInstanceId(instanceId);
        String type = PROMETHEUS_POD.getType(modelId);
        Object dataByType = selectDropChangeManage.getDataByType(type, instanceId);
        List<PrometheusNameSpaceSelectParam> list = (List<PrometheusNameSpaceSelectParam>) dataByType;
        return Reply.ok(list);
    }


    private void getSyncData(Integer serverId, Integer relationId, List<PrometheusInstanceParam> prometheusInstanceList, String queryType, PrometheusContainerEnum prometheusEnum,
                             List<AddAndUpdateModelInstanceParam> allInsertInstanceInfo, List<PrometheusInstanceParam> allDeleteInstanceInfo) throws Exception {
        PrometheusApiConnectorImpl prometheusApiConnector = PrometheusApiConnectorFactory.createConnector(serverId);
        PanelQueryParamVo panelQueryParamVo = new PanelQueryParamVo();
        panelQueryParamVo.setQuery(queryType);
        //获取所有nameSpace数据
        PrometheusResponseVo nameSpaceResponseVo = prometheusApiConnector.doQuery(panelQueryParamVo);
        if (nameSpaceResponseVo == null) {
            throw new SyncConnectException("连接失败!");
        }
        List<PrometheusInstanceParam> nameSpaceNewList = resultConvert(nameSpaceResponseVo, prometheusEnum.getType(), serverId);
        //获取es中类型为nameSpace的数据
        List<PrometheusInstanceParam> nameSpaceOldList = prometheusInstanceList.stream().filter(s -> prometheusEnum.getType().equals(s.getType())).collect(Collectors.toList());
        //数据校验
        List<PrometheusInstanceParam> addNameSpaceList = checkAddCompare(nameSpaceNewList, nameSpaceOldList);
        List<PrometheusInstanceParam> deleteNameSpaceList = checkDeleteCompare(nameSpaceNewList, nameSpaceOldList);
        List<AddAndUpdateModelInstanceParam> nameSpaceInstanceList = mwModelInstanceService.convertInstanceList(addNameSpaceList, prometheusEnum.getModelId(), relationId);
        allInsertInstanceInfo.addAll(nameSpaceInstanceList);
        allDeleteInstanceInfo.addAll(deleteNameSpaceList);
    }


    private List<PrometheusInstanceParam> resultConvert(PrometheusResponseVo nameSpaceResponseVo, String type, Integer serverId) {
        List<PrometheusInstanceParam> ts = new ArrayList<>();
        if (nameSpaceResponseVo != null && SUCCESS.equals(nameSpaceResponseVo.getStatus())) {
            List<PrometheusResultVo> result = nameSpaceResponseVo.getData().getResult();
            for (PrometheusResultVo resultVo : result) {
                Map<String, String> metric = resultVo.getMetric();
                if (metric != null) {
                    PrometheusInstanceParam t = JSONObject.parseObject(JSONObject.toJSONString(metric), PrometheusInstanceParam.class);
                    t.setType(type);
                    t.setMonitorServerId(serverId);
                    if (PROMETHEUS_POD.getType().equals(type)) {
                        t.setInstanceName(t.getPod());
                        //区分唯一的标识值
                        t.setCheckKey(t.getNamespace() + "_" + t.getPod());
                    } else if (PROMETHEUS_CONTAINER.getType().equals(type)) {
                        t.setInstanceName(t.getContainer());
                        t.setCheckKey(t.getNamespace() + "_" + t.getPod() + "_" + t.getContainer());
                    } else {
                        t.setInstanceName(t.getNamespace());
                        t.setCheckKey(t.getNamespace());
                    }
                    ts.add(t);
                }
            }
        }
        return ts;
    }

    /**
     * 旧数据Map和新List比较，如果新数据的key在oldMap中没有，则要该条数据新增
     *
     * @param newList
     * @param oldList
     */
    private List<PrometheusInstanceParam> checkAddCompare(List<PrometheusInstanceParam> newList, List<PrometheusInstanceParam> oldList) {
        Map<String, PrometheusInstanceParam> oldMap = oldList.stream().collect(Collectors.toMap(s -> s.getCheckKey(), s -> s));
        if (CollectionUtils.isEmpty(oldList)) {
            return newList;
        }
        List<PrometheusInstanceParam> addList = new ArrayList<>();
        for (PrometheusInstanceParam instanceParam : newList) {
            if (!oldMap.containsKey(instanceParam.getCheckKey())) {
                addList.add(instanceParam);
            }
        }
        return addList;
    }


    /**
     * 新数据map和旧数据list比较，如果旧数据在新的map中不存在，则删除
     *
     * @param newList
     * @param oldList
     * @return
     */
    private List<PrometheusInstanceParam> checkDeleteCompare(List<PrometheusInstanceParam> newList, List<PrometheusInstanceParam> oldList) {
        Map<String, PrometheusInstanceParam> newMap = newList.stream().collect(Collectors.toMap(s -> s.getCheckKey(), s -> s));
        if (CollectionUtils.isEmpty(newList)) {
            return null;
        }
        List<PrometheusInstanceParam> deleteList = new ArrayList<>();
        for (PrometheusInstanceParam instanceParam : oldList) {
            if (!newMap.containsKey(instanceParam.getCheckKey())) {
                deleteList.add(instanceParam);
            }
        }
        return deleteList;
    }

}
