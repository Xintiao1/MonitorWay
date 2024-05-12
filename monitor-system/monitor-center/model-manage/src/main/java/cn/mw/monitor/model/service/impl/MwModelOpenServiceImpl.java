package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.model.dao.MWModelCommonDao;
import cn.mw.monitor.model.dao.MwModelInstanceDao;
import cn.mw.monitor.model.service.MwModelOpenService;
import cn.mw.monitor.service.model.param.*;
import cn.mw.monitor.service.model.service.MwModelCommonService;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static cn.mw.monitor.service.model.service.ModelCabinetField.*;
import static cn.mw.monitor.service.model.service.MwModelViewCommonService.*;
import static cn.mw.monitor.service.model.util.ValConvertUtil.intValueConvert;
import static cn.mw.monitor.service.model.util.ValConvertUtil.strValueConvert;

/**
 * @author qzg
 * @date 2023/8/3
 */
@Slf4j
@Service
public class MwModelOpenServiceImpl implements MwModelOpenService {
    @Resource
    private MWModelCommonDao mwModelCommonDao;
    @Resource
    private MwModelInstanceDao mwModelInstanceDao;
    @Autowired
    private MwModelCommonService mwModelCommonService;
    @Autowired
    private MwModelViewServiceImpl mwModelViewServiceImpl;

    @Value("${mw.link.modelId}")
    private Integer linkModelId;

    @Value("${mw.model.desc}")
    private String modelDesc;

    static final String ABNORMAL = "ABNORMAL";
    static final String NORMAL = "NORMAL";
    static final String UNKNOWN = "UNKNOWN";

    @Override
    public Reply getRoomInfoByDigitalTwin() {
        List<MwModelDigitalTwinRoomParam> roomParamList = new ArrayList<>();
        try {
            long time1 = System.currentTimeMillis();
            roomParamList = getRoomInfo();
            List<MwModelDigitalTwinCabinetParam> cabinetParamList = new ArrayList<>();
            long time2 = System.currentTimeMillis();
            if (CollectionUtils.isNotEmpty(roomParamList)) {
                for (MwModelDigitalTwinRoomParam roomParam : roomParamList) {
                    if (CollectionUtils.isNotEmpty(roomParam.getCabinets())) {
                        Set<String> cabinetIds = roomParam.getCabinets().stream().map(s -> s.getId()).collect(Collectors.toSet());
                        List<MwModelDigitalTwinCabinetParam> cabinetInfoList = getCabinetInfo(new ArrayList<>(cabinetIds));
                        roomParam.setCabinets(cabinetInfoList);
                    }
                }
            }
            long time4 = System.currentTimeMillis();
            log.info("耗时111::" + (time2 - time1) + "ms" + (time4 - time2) + "ms：roomParamList::" + roomParamList);
        } catch (Exception e) {
            log.error("getRoomInfoByDigitalTwin to fail::", e);
        }
        return Reply.ok(roomParamList);
    }

    @Override
    public Reply getLinkInfo() {
        //获取mysql链路信息
        List<MwModelInstanceCommonParam> allLinkInfo = mwModelInstanceDao.selectModelInstanceInfoById(linkModelId);
        log.info("数据库查询获取链路数据::" + allLinkInfo);
        Set<Integer> instanceIds = allLinkInfo.stream().map(s -> s.getModelInstanceId()).collect(Collectors.toSet());
        QueryEsParam queryEsParam = new QueryEsParam();
        QueryModelInstanceByPropertyIndexParam qParam = new QueryModelInstanceByPropertyIndexParam();
        //通过链路的Id，查询所有链路信息
        //本端，对端机柜都能查询到链路数据
        qParam.setPropertiesIndexId(INSTANCE_ID_KEY);
        qParam.setPropertiesValueList(new ArrayList(instanceIds));
        queryEsParam.setParamLists(Arrays.asList(qParam));
        //查询链路设备信息
        List<Map<String, Object>> linkMapList = mwModelCommonService.getAllInstanceInfoByQueryParam(queryEsParam);

        List<MwModelLinkDeviceParam> linkDataList = JSONArray.parseArray(JSONArray.toJSONString(linkMapList), MwModelLinkDeviceParam.class);
        for (MwModelLinkDeviceParam m : linkDataList) {
            MwModelDigitalTwinLinkParam param = new MwModelDigitalTwinLinkParam();
            param.setLinkId(strValueConvert(m.getModelInstanceId()));
            if (CollectionUtils.isNotEmpty(m.getMultiNode())) {
                List<String> multiNodes = m.getMultiNode();
                multiNodes.add(0, m.getOwnCabinetPosition());
                multiNodes.add(m.getOppositeCabinetPosition());
                String multiNode = String.join(",", multiNodes);
                param.setMultiNode(multiNode);
            }
        }

        List<Map<Integer, List<MwModelLinkDeviceParam>>> list = new ArrayList();
        //数据组装
        //根据本端机柜Id分组
        Map<Integer, List<MwModelLinkDeviceParam>> ownCabinetIdCollect = linkDataList.stream().filter(s -> s.getOwnLinkCabinetId() != null).collect(Collectors.groupingBy(s -> s.getOwnLinkCabinetId()));
        list.add(ownCabinetIdCollect);
        //根据对端机柜Id分组
        Map<Integer, List<MwModelLinkDeviceParam>> oppositeCabinetIdCollect = linkDataList.stream().filter(s -> s.getOppositeLinkCabinetId() != null).collect(Collectors.groupingBy(s -> s.getOppositeLinkCabinetId()));
        list.add(oppositeCabinetIdCollect);
        //根据本端设备Id分组
        Map<Integer, List<MwModelLinkDeviceParam>> ownDeviceIdCollect = linkDataList.stream().filter(s -> s.getOwnLinkDeviceId() != null).collect(Collectors.groupingBy(s -> s.getOwnLinkDeviceId()));
        list.add(ownDeviceIdCollect);
        //根据对端设备Id分组
        Map<Integer, List<MwModelLinkDeviceParam>> oppositeDeviceIdIdCollect = linkDataList.stream().filter(s -> s.getOppositeLinkDeviceId() != null).collect(Collectors.groupingBy(s -> s.getOppositeLinkDeviceId()));
        list.add(oppositeDeviceIdIdCollect);

        Map<Integer, List<MwModelLinkDeviceParam>> collect = list.stream().flatMap(map -> map.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existingList, newList) -> {
            existingList.addAll(newList);
            return existingList;
        }));
        return Reply.ok(collect);
    }


    @Override
    public Reply getAllLinkInfo() {
        List<MwModelDigitalTwinLinkParam> listLinkData = new ArrayList<>();
        //获取mysql链路信息
        List<MwModelInstanceCommonParam> allLinkInfo = mwModelInstanceDao.selectModelInstanceInfoById(linkModelId);
        log.info("数据库查询获取链路数据::" + allLinkInfo);
        Set<Integer> instanceIds = allLinkInfo.stream().map(s -> s.getModelInstanceId()).collect(Collectors.toSet());
        QueryEsParam queryEsParam = new QueryEsParam();
        QueryModelInstanceByPropertyIndexParam qParam = new QueryModelInstanceByPropertyIndexParam();
        //通过链路的Id，查询所有链路信息
        qParam.setPropertiesIndexId(INSTANCE_ID_KEY);
        qParam.setPropertiesValueList(new ArrayList(instanceIds));
        queryEsParam.setParamLists(Arrays.asList(qParam));
        //查询链路设备信息
        List<Map<String, Object>> linkMapList = mwModelCommonService.getAllInstanceInfoByQueryParam(queryEsParam);

        List<MwModelLinkDeviceParam> linkDataList = JSONArray.parseArray(JSONArray.toJSONString(linkMapList), MwModelLinkDeviceParam.class);
        Map<Integer, MwModelDigitalTwinLinkParam> linkMap = new HashMap<>();
        for (MwModelLinkDeviceParam m : linkDataList) {
            MwModelDigitalTwinLinkParam param = new MwModelDigitalTwinLinkParam();
            param.setLinkId(strValueConvert(m.getModelInstanceId()));
//            if (CollectionUtils.isNotEmpty(m.getMultiNode())) {
            List<String> multiNodes = m.getMultiNode();
            multiNodes.add(0, m.getOwnCabinetPosition());
            multiNodes.add(m.getOppositeCabinetPosition());
            String multiNode = String.join(",", multiNodes);
            param.setMultiNode(multiNode);
            param.setLinkType(m.getLinkType());
//            }
            linkMap.put(m.getModelInstanceId(), param);
        }

        //获取链路数据中的机柜下的设备
        Map<Integer, Set<Integer>> deviceIdsByCabinetId = linkDataList.stream().filter(s -> s.getOwnLinkCabinetId() != null && s.getOwnLinkDeviceId() != null).
                collect(Collectors.groupingBy(s -> s.getOwnLinkCabinetId(), Collectors.mapping(s -> s.getOwnLinkDeviceId(), Collectors.toSet())));

        //获取设备Id下的链路数据
        Map<Integer, Set<Integer>> linkIdsByDeviceId = linkDataList.stream().filter(s -> s.getOwnLinkDeviceId() != null && s.getModelInstanceId() != null).
                collect(Collectors.groupingBy(s -> s.getOwnLinkDeviceId(), Collectors.mapping(s -> s.getModelInstanceId(), Collectors.toSet())));

        List<MwModelDigitalTwinCabinetLinkParam> cabinetLinkList = new ArrayList<>();
        deviceIdsByCabinetId.forEach((k, v) -> {
            MwModelDigitalTwinCabinetLinkParam cabinetLinkParam = new MwModelDigitalTwinCabinetLinkParam();
            Integer cabinetId = k;
            cabinetLinkParam.setCabinetId(strValueConvert(cabinetId));
            List<Integer> deviceIds = new ArrayList<>(v);
            List<MwModelDigitalTwinDeviceLinkParam> deviceLinkList = new ArrayList<>();
            for (Integer deviceId : deviceIds) {
                MwModelDigitalTwinDeviceLinkParam deviceLinkParam = new MwModelDigitalTwinDeviceLinkParam();
                deviceLinkParam.setDeviceId(strValueConvert(deviceId));
                if (linkIdsByDeviceId != null && linkIdsByDeviceId.containsKey(deviceId)) {
                    Set<Integer> linkIds = linkIdsByDeviceId.get(deviceId);
                    List<MwModelDigitalTwinLinkParam> linkInfoList = new ArrayList<>();
                    for (Integer linkId : linkIds) {
                        if (linkMap != null && linkMap.containsKey(linkId)) {
                            MwModelDigitalTwinLinkParam linkInfo = linkMap.get(linkId);
                            linkInfoList.add(linkInfo);
                        }
                    }
                    deviceLinkParam.setLinkList(linkInfoList);
                    deviceLinkList.add(deviceLinkParam);
                }
            }
            cabinetLinkParam.setDeviceList(deviceLinkList);
            cabinetLinkList.add(cabinetLinkParam);
        });

        return Reply.ok(cabinetLinkList);
    }

    private List<MwModelDigitalTwinRoomParam> getRoomInfo() {
        List<MwModelDigitalTwinRoomParam> list = new ArrayList<>();
        QueryEsParam queryEsParam = new QueryEsParam();
        QueryModelInstanceByPropertyIndexParam qParam = new QueryModelInstanceByPropertyIndexParam();
        MwModelInstanceCommonParam param = new MwModelInstanceCommonParam();
        param.setModelDesc(modelDesc);
        List<MwModelInstanceCommonParam> allRoomInfo = mwModelInstanceDao.getAllRoomInfo(param);
        Set<Integer> instanceIds = allRoomInfo.stream().map(s -> s.getModelInstanceId()).collect(Collectors.toSet());
        qParam.setPropertiesIndexId(INSTANCE_ID_KEY);
        qParam.setPropertiesValueList(new ArrayList(instanceIds));
        queryEsParam.setParamLists(Arrays.asList(qParam));
        //查询机房设备信息
        List<Map<String, Object>> roomListMap = mwModelCommonService.getAllInstanceInfoByQueryParam(queryEsParam);
        Map<Integer, Map<String, Object>> roomInfoMap = roomListMap.stream().collect(Collectors.toMap(s -> intValueConvert(s.get(INSTANCE_ID_KEY)), s -> s));

        //查询机房下的机柜设备
        qParam = new QueryModelInstanceByPropertyIndexParam();
        qParam.setPropertiesIndexId(RELATIONSITEROOM.getField());
        qParam.setPropertiesValueList(new ArrayList(instanceIds));
        //机柜设备必须存在U位数字段
        queryEsParam = new QueryEsParam();
        queryEsParam.setExistsList(Arrays.asList(UNUM.getField()));
        queryEsParam.setParamLists(Arrays.asList(qParam));
        List<Map<String, Object>> cabinetListmap = mwModelCommonService.getAllInstanceInfoByQueryParam(queryEsParam);

        Map<Integer, List<Map<String, Object>>> collect = cabinetListmap.stream().collect(Collectors.groupingBy(s -> intValueConvert(s.get(RELATIONSITEROOM.getField()))));

        collect.forEach((k, v) -> {
            MwModelDigitalTwinRoomParam roomParam = new MwModelDigitalTwinRoomParam();
            Integer roomId = k;
            if (roomInfoMap != null && roomInfoMap.containsKey(k)) {
                Map<String, Object> roomInfo = roomInfoMap.get(k);
                String id = strValueConvert(roomInfo.get(INSTANCE_ID_KEY));
                //机房名称
                String name = strValueConvert(roomInfo.get(INSTANCE_NAME_KEY));
                //机房CODE
                String code = strValueConvert(roomInfo.get(INSTANCE_CODE));
                roomParam.setId(id);
                roomParam.setName(name);
                roomParam.setModel(code);
            }
            List<Map<String, Object>> mapList = v;
            List<MwModelDigitalTwinCabinetParam> cabinetParamList = new ArrayList<>();
            for (Map<String, Object> m : mapList) {
                MwModelDigitalTwinCabinetParam cabinetParam = new MwModelDigitalTwinCabinetParam();
                //机柜Id
                cabinetParam.setId(strValueConvert(m.get(INSTANCE_ID_KEY)));
                //机柜名称
                cabinetParam.setName(strValueConvert(m.get(INSTANCE_NAME_KEY)));
                //机柜位置要求：2-1-A01、2-1-F11；2-1为机房编号
                // 此处位置为机柜编号：2-1-A01 B-201-01
                cabinetParam.setPosition(strValueConvert(m.get(INSTANCE_CODE)));
                cabinetParamList.add(cabinetParam);
            }
            roomParam.setCabinets(cabinetParamList);
            list.add(roomParam);
        });
        return list;
    }


    private List<MwModelDigitalTwinCabinetParam> getCabinetInfo(List<String> ids) {
        QueryEsParam queryEsParam = new QueryEsParam();
        QueryModelInstanceByPropertyIndexParam qParam = new QueryModelInstanceByPropertyIndexParam();
        qParam.setPropertiesIndexId(INSTANCE_ID_KEY);
        qParam.setPropertiesValueList(ids);
        queryEsParam.setParamLists(Arrays.asList(qParam));
        List<MwModelDigitalTwinCabinetParam> cabinetList = new ArrayList<>();
        //查询机柜设备信息
        List<Map<String, Object>> cabinetListMap = mwModelCommonService.getAllInstanceInfoByQueryParam(queryEsParam);
        if (CollectionUtils.isNotEmpty(cabinetListMap)) {
            for (Map<String, Object> cabinetMap : cabinetListMap) {
                MwModelDigitalTwinCabinetParam cabinetParam = new MwModelDigitalTwinCabinetParam();
                //机房名称
                String cabinetName = strValueConvert(cabinetMap.get(INSTANCE_NAME_KEY));
                String position = strValueConvert(cabinetMap.get(INSTANCE_CODE));
                cabinetParam.setId(strValueConvert(cabinetMap.get(INSTANCE_ID_KEY)));
                cabinetParam.setPosition(position);
                cabinetParam.setName(cabinetName);
                cabinetList.add(cabinetParam);
            }
        }
        cabinetList = cabinetList.stream().sorted(Comparator.comparing(s -> strValueConvert(s.getPosition()))).collect(Collectors.toList());
        Map<String, String> cabinetNameMapById = cabinetList.stream().collect(Collectors.toMap(s -> s.getId(), s -> s.getName()));

        //查询机柜下的资产设备
        qParam.setPropertiesIndexId(RELATIONSITECABINET.getField());
        qParam.setPropertiesValueList(ids);
        queryEsParam.setParamLists(Arrays.asList(qParam));
        List<Map<String, Object>> deviceList = mwModelCommonService.getAllInstanceInfoByQueryParam(queryEsParam);
        List<Map<String, Object>> newList = mwModelViewServiceImpl.getAssetsStateByZabbix(deviceList);
        List<MwModelDigitalTwinDeviceParam> deviceParamList = new ArrayList<>();
        for (Map<String, Object> m : newList) {
            MwModelDigitalTwinDeviceParam deviceParam = new MwModelDigitalTwinDeviceParam();
            //设备Id
            deviceParam.setId(strValueConvert(m.get(INSTANCE_ID_KEY)));
            //设备名称
            deviceParam.setName(strValueConvert(m.get(INSTANCE_NAME_KEY)));
            deviceParam.setType(strValueConvert(m.get(ASSETSUBTYPE_NAME)));
            deviceParam.setIp(strValueConvert(m.get(IN_BAND_IP)));
            deviceParam.setMaker(strValueConvert(m.get(MANUFACTURER)));
            deviceParam.setModel(strValueConvert(m.get(SPECIFICATIONS)));
            deviceParam.setSerialNum(strValueConvert(m.get(SERIAL_NUM)));
            if (m.get(POSITIONBYCABINET.getField()) != null) {
                CabinetLayoutDataParam cabinetCoordinate = JSONObject.parseObject(JSONObject.toJSONString(m.get(POSITIONBYCABINET.getField())), CabinetLayoutDataParam.class);
                deviceParam.setHigh(intValueConvert(cabinetCoordinate.getEnd()) - intValueConvert(cabinetCoordinate.getStart()) + 1);
                deviceParam.setPosition(intValueConvert(cabinetCoordinate.getStart()) + 1);
            }
           String cabinetId = strValueConvert(m.get(RELATIONSITECABINET.getField()));
            if(cabinetNameMapById!=null && cabinetNameMapById.containsKey(cabinetId)){
                deviceParam.setRelationCabinetName(strValueConvert(cabinetNameMapById.get(cabinetId)));
            }
            deviceParam.setCabinetId(strValueConvert(m.get(RELATIONSITECABINET.getField())));
            int state = 0;
            String status = strValueConvert(m.get(ITEM_ASSETS_STATUS));
            if (NORMAL.equals(status)) {
                state = 1;
            }
            if (ABNORMAL.equals(status)) {
                state = 2;
            }
            deviceParam.setState(state);
            deviceParamList.add(deviceParam);
        }
        deviceParamList = deviceParamList.stream().sorted(Comparator.comparing(s -> s.getPosition())).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(deviceParamList)) {
            Map<String, List<MwModelDigitalTwinDeviceParam>> deviceMap = deviceParamList.stream().filter(s -> !Strings.isNullOrEmpty(s.getCabinetId())).collect(Collectors.groupingBy(s -> s.getCabinetId()));
            for (MwModelDigitalTwinCabinetParam cabinetParam : cabinetList) {
                if (deviceMap != null && deviceMap.containsKey(cabinetParam.getId())) {
                    cabinetParam.setDevices(deviceMap.get(cabinetParam.getId()));
                }
            }
        }
        return cabinetList;
    }

    private MwModelDigitalTwinDeviceParam getDeviceInfoById(String id) {
        MwModelDigitalTwinDeviceParam deviceParam = new MwModelDigitalTwinDeviceParam();
        QueryEsParam queryEsParam = new QueryEsParam();
        QueryModelInstanceByPropertyIndexParam qParam = new QueryModelInstanceByPropertyIndexParam();
        qParam.setPropertiesIndexId(INSTANCE_ID_KEY);
        qParam.setPropertiesValueList(Arrays.asList(intValueConvert(id)));
        queryEsParam.setParamLists(Arrays.asList(qParam));
        //查询机柜设备信息
        List<Map<String, Object>> deviceListMap = mwModelCommonService.getAllInstanceInfoByQueryParam(queryEsParam);
        List<Map<String, Object>> newList = mwModelViewServiceImpl.getAssetsStateByZabbix(deviceListMap);
        if (CollectionUtils.isNotEmpty(newList)) {
            Map m = newList.get(0);
            //设备Id
            deviceParam.setId(strValueConvert(m.get(INSTANCE_ID_KEY)));
            //设备名称
            deviceParam.setName(strValueConvert(m.get(INSTANCE_NAME_KEY)));
            deviceParam.setType(strValueConvert(m.get(ASSETSUBTYPE_NAME)));
            deviceParam.setIp(strValueConvert(m.get(IN_BAND_IP)));
            deviceParam.setMaker(strValueConvert(m.get(MANUFACTURER)));
            deviceParam.setModel(strValueConvert(m.get(SPECIFICATIONS)));
            deviceParam.setSerialNum(strValueConvert(m.get(SERIAL_NUM)));
            if (m.get(POSITIONBYCABINET.getField()) != null) {
                CabinetLayoutDataParam cabinetCoordinate = JSONObject.parseObject(JSONObject.toJSONString(m.get(POSITIONBYCABINET.getField())), CabinetLayoutDataParam.class);
                deviceParam.setHigh(intValueConvert(cabinetCoordinate.getEnd()) - intValueConvert(cabinetCoordinate.getStart()) + 1);
                deviceParam.setPosition(intValueConvert(cabinetCoordinate.getStart()) + 1);
            }
            deviceParam.setCabinetId(strValueConvert(m.get(RELATIONSITECABINET.getField())));
            int state = 0;
            String status = strValueConvert(m.get(ITEM_ASSETS_STATUS));
            if (NORMAL.equals(status)) {
                state = 1;
            }
            if (ABNORMAL.equals(status)) {
                state = 2;
            }
            deviceParam.setState(state);
        }
        return deviceParam;
    }
}
