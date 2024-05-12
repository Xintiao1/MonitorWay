package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.model.dao.MwModelInstanceDao;
import cn.mw.monitor.model.dao.MwModelViewDao;
import cn.mw.monitor.model.service.MwModelDigitalTwinService;
import cn.mw.monitor.model.service.MwModelInstanceService;
import cn.mw.monitor.model.service.MwModelOpenService;
import cn.mw.monitor.model.util.ModelOKHttpUtils;
import cn.mw.monitor.service.model.dto.ModelInstanceBaseInfoDTO;
import cn.mw.monitor.service.model.param.*;
import cn.mw.monitor.service.model.service.MwModelCommonService;
import cn.mw.monitor.service.visualized.api.MwVisualizedCommonService;
import cn.mw.monitor.service.visualized.dto.MwDigitalTwinAlertDto;
import cn.mw.monitor.service.visualized.dto.MwDigitalTwinItemDto;
import cn.mw.monitor.service.visualized.param.MwDigitalTwinItemParam;
import cn.mwpaas.common.constant.PaasConstant;
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

import static cn.mw.monitor.model.service.impl.MwModelSuperFusionServiceImpl.HTTP_SUCCESS;
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
public class MwModelDigitalTwinServiceImpl implements MwModelDigitalTwinService {
    @Resource
    private MwModelInstanceDao mwModelInstanceDao;
    @Autowired
    private MwModelInstanceService mwModelInstanceService;
    @Autowired
    private MwModelCommonService mwModelCommonService;
    @Autowired
    private MwVisualizedCommonService mwVisualizedCommonService;
    @Autowired
    private MwModelViewServiceImpl mwModelViewServiceImpl;

    @Autowired
    private MwModelOpenService mwModelOpenService;

    @Resource
    private MwModelViewDao mwModelViewDao;

    @Value("${city.adcode}")
    private String adcode;
    @Value("${gaode.key}")
    private String apiKey;
    @Value("${mw.link.modelId}")
    private Integer linkModelId;
    @Value("${mw.floor.modelId}")
    private Integer floorModelId;

    static final String interfaceUp = "up";

    static final String interfaceDown = "down";

    static final String ABNORMAL = "ABNORMAL";
    static final String NORMAL = "NORMAL";
    static final String UNKNOWN = "UNKNOWN";
    static final String ROOM = "room";//机房
    static final String CABINET = "cabinet";//机柜
    static final String DEVICE = "device";//设备
    static final String LINK = "link";//链路
    static final String LOOKLINK = "lookLink";//查看链路
    static final String BUILDING = "building";//楼宇
    static final String BUILD_LOCATION = "buildLocation";
    static final String BUILDINGADDRESS = "民航科技创新示范区A02";//楼宇地址
    private String buildName = "A2地块-数据中心";


    private String roomName = "";
    private String cabinetName = "";
    private String linkName = "";
    private String deviceUNum = "";
    private String lookLinkName = "查看线缆";



    @Override
    public Reply getAlertShowInfo(MwModelAlertShowParam param) {
        QueryDigitalTwinShowParam showParam = new QueryDigitalTwinShowParam();
        String id = param.getId();
        String type = "";
        if("-1".equals(id)){
            type = LOOKLINK;
        }else{
            type = getModelTypeById(param.getId());
        }
        showParam.setPageType(type);
        try {
            if (ROOM.equals(type)) {//机房信息
                long time10 = System.currentTimeMillis();
                String currentLocation = getCurrentLocaltion(type);
                showParam.setCurrentLocation(currentLocation);
                long time11 = System.currentTimeMillis();
                List<String> cabinetIds = getRoomInfoAndCabinetIdsById(Arrays.asList(intValueConvert(id)), showParam);
                long time12 = System.currentTimeMillis();
                if (CollectionUtils.isNotEmpty(cabinetIds)) {
//                    //根据机柜的Ids，获取所有的机柜信息
//                    getCabinetInfo(new ArrayList<>(cabinetIds), showParam);
//                    long time13 = System.currentTimeMillis();
                    //查询机柜下的资产设备列表
                    List<MwModelAlertShowDeviceParam> deviceList = getDeviceListByCabinetIds(new ArrayList<>(cabinetIds));
                    long time14 = System.currentTimeMillis();
                    //获取设备信息
                    getDigitalTwinShowByDevice(deviceList, showParam);
                    long time15 = System.currentTimeMillis();
                    //设备参数转换，获取监控主机Id和监控服务器Id
                    List<MwDigitalTwinItemParam> listParam = getDeviceAssetsIdInfo(deviceList);
                    //调用可视化接口，获取告警信息
                    MwDigitalTwinAlertDto alarmParam = mwVisualizedCommonService.getAssetsAlertInfo(listParam);
                    long time16 = System.currentTimeMillis();
                    showParam.setAlarmInfo(alarmParam);
                    log.info("机房页面信息耗时:time1:" + (time11 - time10) + "ms;time2:" + (time12 - time11) + "ms;time5:" + (time15 - time14) + "ms;time6:" + (time16 - time15) + "ms;");
                }
            } else if (CABINET.equals(type)) {//机柜页面
                long time20 = System.currentTimeMillis();
                //机柜信息
                getCabinetInfo(Arrays.asList(id), showParam);
                long time21 = System.currentTimeMillis();
                cabinetName = mwModelInstanceDao.getInstanceNameById(intValueConvert(id));
                String currentLocation = getCurrentLocaltion(type);
                showParam.setCurrentLocation(currentLocation);
                //查询机柜下的资产设备列表
                List<MwModelAlertShowDeviceParam> deviceList = getDeviceListByCabinetIds(Arrays.asList(id));
                long time22 = System.currentTimeMillis();
                //获取设备信息
                getDigitalTwinShowByDevice(deviceList, showParam);
                long time23 = System.currentTimeMillis();
                //设备参数转换，获取监控主机Id和监控服务器Id
                List<MwDigitalTwinItemParam> listParam = getDeviceAssetsIdInfo(deviceList);
                //获取告警信息
                MwDigitalTwinAlertDto alarmParam = mwVisualizedCommonService.getAssetsAlertInfo(listParam);
                long time24 = System.currentTimeMillis();
                showParam.setAlarmInfo(alarmParam);
                log.info("机柜页面信息耗时:time1:" + (time21 - time20) + "ms;time2:" + (time22 - time21) + "ms;time3:" + (time23 - time22) + "ms;time4:" + (time24 - time23) + "ms;");
            } else if (DEVICE.equals(type)) {
                long time30 = System.currentTimeMillis();
                //设备信息
                List<MwModelAlertShowDeviceParam> deviceList = getDeviceInfoById(id);
                long time31 = System.currentTimeMillis();
                if (CollectionUtils.isNotEmpty(deviceList)) {
                    deviceUNum = deviceList.get(0).getPosition() + "U";
                    String currentLocation = getCurrentLocaltion(type);
                    showParam.setCurrentLocation(currentLocation);
                }
                List<MwDigitalTwinItemParam> listParam = getDeviceAssetsIdInfo(deviceList);
                long time32 = System.currentTimeMillis();
                //获取告警信息
                MwDigitalTwinAlertDto alarmParam = mwVisualizedCommonService.getAssetsAlertInfo(listParam);
                showParam.setAlarmInfo(alarmParam);
                if (CollectionUtils.isNotEmpty(listParam)) {
                    Reply reply = mwVisualizedCommonService.getAssetsItemInfo(listParam.get(0));
                    if (null != reply && PaasConstant.RES_SUCCESS == reply.getRes()) {
                        List<MwDigitalTwinItemDto> deviceInfoList = (List<MwDigitalTwinItemDto>) reply.getData();
                        if (CollectionUtils.isNotEmpty(deviceInfoList)) {
                            MwDigitalTwinItemDto deviceInfo = deviceInfoList.get(0);
                            showParam.setDeviceInfo(deviceInfo);
                        }
                    }
                }
                long time33 = System.currentTimeMillis();
                log.info("设备页面信息耗时:time1:" + (time31 - time30) + "ms;time2:" + (time32 - time31) + "ms;time3:" + (time33 - time32) + "ms");
            } else if (LINK.equals(type)) {
                linkName = mwModelInstanceDao.getInstanceNameById(intValueConvert(id));
                String currentLocation =  getCurrentLocaltion(type);
                showParam.setCurrentLocation(currentLocation);
            }else if (LOOKLINK.equals(type)) {
                String currentLocation =  getCurrentLocaltion(type);
                showParam.setCurrentLocation(currentLocation);
            }else {
                //楼宇信息
                //id为空，获取所有楼宇信息
                long time40 = System.currentTimeMillis();
                List<Integer> roomIds = getBuildInfoAndRoomIdsById(id, showParam);
                long time41 = System.currentTimeMillis();
                List<String> cabinetIds = getRoomInfoAndCabinetIdsById(roomIds, showParam);
                long time42 = System.currentTimeMillis();
                long time43 = 0l;
                long time44 = 0l;
                long time45 = 0l;
                if (CollectionUtils.isNotEmpty(cabinetIds)) {
//                    //机柜信息
//                    getCabinetInfo(cabinetIds, showParam);
//                    time43 = System.currentTimeMillis();
                    //查询机柜下的资产设备列表
                    List<MwModelAlertShowDeviceParam> deviceList = getDeviceListByCabinetIds(new ArrayList<>(cabinetIds));
                    //获取设备信息
                    getDigitalTwinShowByDevice(deviceList, showParam);
                    time44 = System.currentTimeMillis();
                    //设备参数转换，获取监控主机Id和监控服务器Id
                    List<MwDigitalTwinItemParam> listParam = getDeviceAssetsIdInfo(deviceList);
                    //获取告警信息
                    MwDigitalTwinAlertDto alarmParam = mwVisualizedCommonService.getAssetsAlertInfo(listParam);
                    time45 = System.currentTimeMillis();
                    showParam.setAlarmInfo(alarmParam);
                }
                //获取天气
                long time46 = System.currentTimeMillis();
                getWeatherInfoByCity(showParam);
                long time47 = System.currentTimeMillis();
                showParam.setCurrentLocation(buildName);
                log.info("楼宇页面信息耗时:time1:" + (time41 - time40) + "ms;time2:" + (time42 - time41) + "ms;time3:" + (time43 - time42) + "ms;time4:" + (time44 - time43) + "ms;time5:" + (time45 - time44) + "ms;time6:" + (time47 - time46) + "ms;");
            }
        } catch (Exception e) {
            log.error("getRoomInfoByDigitalTwin to fail::", e);
        }
        return Reply.ok(showParam);
    }

    @Override
    public Reply getPageTypeInfoById(MwModelAlertShowParam param) {
        String type = "building";
        try {
            type = getModelTypeById(param.getId());
        } catch (Exception e) {
            log.error("getPageTypeInfoById to fail::", e);
        }
        return Reply.ok(type);
    }

    @Override
    public Reply getLinkInfoById(MwModelAlertShowParam param) {
        QueryDigitalTwinLinkParam queryDigitalTwinLinkParam = new QueryDigitalTwinLinkParam();
        try {
            List<MwModelLinkDeviceParam> allList = new ArrayList<>();
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
//                if (CollectionUtils.isNotEmpty(m.getMultiNode())) {
                    List<String> multiNodes = m.getMultiNode();
                    multiNodes.add(0, m.getOwnLinkDeviceName());
                    multiNodes.add(1, m.getOwnLinkCabinetName());
                    multiNodes.add(m.getOppositeLinkCabinetName());
                    multiNodes.add(m.getOppositeLinkDeviceName());
                    m.setMultiNode(multiNodes);
//                }
            }

            String type = getModelTypeById(param.getId());
            if (ROOM.equals(type)) {//机房信息
                queryEsParam = new QueryEsParam();
                qParam = new QueryModelInstanceByPropertyIndexParam();
                List<QueryModelInstanceByPropertyIndexParam> paramLists = new ArrayList<>();
                //查询mysql的所有的机柜数据
                List<MwModelInstanceCommonParam> allCabinetInfo = mwModelInstanceDao.getCabinetInfoByRoomId(intValueConvert(param.getId()));
                List<Integer> cabinetIdList = allCabinetInfo.stream().map(s -> s.getModelInstanceId()).collect(Collectors.toList());

                //查询机房下的机柜设备
                qParam.setPropertiesIndexId(RELATIONSITEROOM.getField());
                qParam.setPropertiesValueList(Arrays.asList(param.getId()));
                paramLists.add(qParam);
                qParam = new QueryModelInstanceByPropertyIndexParam();
                qParam.setPropertiesIndexId(INSTANCE_ID_KEY);
                qParam.setPropertiesValueList(cabinetIdList);
                paramLists.add(qParam);
                queryEsParam.setParamLists(paramLists);

                List<Map<String, Object>> cabinetListMap = mwModelCommonService.getAllInstanceInfoByQueryParam(queryEsParam);
                Set<Integer> cabinetIds = cabinetListMap.stream().map(s -> intValueConvert(s.get(INSTANCE_ID_KEY))).collect(Collectors.toSet());
                //获取机房下所有机柜Id
                //根据本端机柜Id分组
                Map<Integer, List<MwModelLinkDeviceParam>> collect = linkDataList.stream().filter(s -> s.getOwnLinkCabinetId() != null).collect(Collectors.groupingBy(s -> s.getOwnLinkCabinetId()));
                for (Integer cabinetId : cabinetIds) {
                    if (collect != null && collect.containsKey(cabinetId)) {
                        List<MwModelLinkDeviceParam> linkInfo = collect.get(cabinetId);
                        allList.addAll(linkInfo);
                    }
                }
                linkInfoConvert(allList,queryDigitalTwinLinkParam);
            } else if (CABINET.equals(type)) {//机柜信息
               String CabinetCode = mwModelInstanceDao.getInstanceNameById(intValueConvert(param.getId()));
                queryDigitalTwinLinkParam.setCabinetCode(CabinetCode);
                //根据本端机柜Id分组
                Map<Integer, List<MwModelLinkDeviceParam>> ownCollect = linkDataList.stream().filter(s -> s.getOwnLinkCabinetId() != null).collect(Collectors.groupingBy(s -> s.getOwnLinkCabinetId()));


                Map<Integer, List<MwModelLinkDeviceParam>> oppoCollect = linkDataList.stream().filter(s -> s.getOppositeLinkCabinetId() != null).collect(Collectors.groupingBy(s -> s.getOppositeLinkCabinetId()));

                Integer id = Integer.valueOf(param.getId());
                //本地机柜id匹配的链路数据
                if (ownCollect != null && ownCollect.containsKey(id)) {
                    List<MwModelLinkDeviceParam> linkInfo = ownCollect.get(id);
                    allList.addAll(linkInfo);
                }
                //对端机柜id匹配的链路数据
                if (oppoCollect != null && oppoCollect.containsKey(id)) {
                    List<MwModelLinkDeviceParam> linkInfo = oppoCollect.get(id);
                    allList.addAll(linkInfo);
                }
                linkInfoConvert(allList,queryDigitalTwinLinkParam);
            } else if (DEVICE.equals(type)) {//设备信息
                //根据本端设备Id分组
                Map<Integer, List<MwModelLinkDeviceParam>> ownCollect = linkDataList.stream().filter(s -> s.getOwnLinkDeviceId() != null).collect(Collectors.groupingBy(s -> s.getOwnLinkDeviceId()));

                Map<Integer, List<MwModelLinkDeviceParam>> oppoCollect = linkDataList.stream().filter(s -> s.getOppositeLinkDeviceId() != null).collect(Collectors.groupingBy(s -> s.getOppositeLinkDeviceId()));

                Integer id = Integer.valueOf(param.getId());
                if (ownCollect != null && ownCollect.containsKey(id)) {
                    List<MwModelLinkDeviceParam> linkInfo = ownCollect.get(id);
                    if(linkInfo!=null && linkInfo.get(0)!=null){
                        queryDigitalTwinLinkParam.setCabinetCode(linkInfo.get(0).getOwnCabinetPosition());
                    }
                    allList.addAll(linkInfo);
                }
                if (oppoCollect != null && oppoCollect.containsKey(id)) {
                    List<MwModelLinkDeviceParam> linkInfo = oppoCollect.get(id);
                    if(linkInfo!=null && linkInfo.get(0)!=null){
                        queryDigitalTwinLinkParam.setCabinetCode(linkInfo.get(0).getOwnCabinetPosition());
                    }
                    allList.addAll(linkInfo);
                }
                linkInfoConvert(allList,queryDigitalTwinLinkParam);
            }
        } catch (Exception e) {
            log.error("getPageTypeInfoById to fail::", e);
        }
        return Reply.ok(queryDigitalTwinLinkParam);
    }


    private void linkInfoConvert(List<MwModelLinkDeviceParam> allList,QueryDigitalTwinLinkParam queryDigitalTwinLinkParam){
        if (CollectionUtils.isNotEmpty(allList)) {
            queryDigitalTwinLinkParam.setLinkNum(allList.size());
            Long RJ45Num = allList.stream().filter(s -> RJ45.equals(s.getLinkType())).count();
            Long MPONum = allList.stream().filter(s -> MPO_MPO.equals(s.getLinkType())).count();
            Long LCNum = allList.stream().filter(s -> LC_LC.equals(s.getLinkType())).count();
            Long upNum = allList.stream().filter(s -> interfaceUp.equals(s.getOwnInterfaceStatus()) || interfaceUp.equals(s.getOppositeInterfaceStatus())).count();
            Long downNum = allList.stream().filter(s -> interfaceDown.equals(s.getOwnInterfaceStatus()) || interfaceDown.equals(s.getOppositeInterfaceStatus())).count();

            queryDigitalTwinLinkParam.setRJ45Num(intValueConvert(RJ45Num));
            queryDigitalTwinLinkParam.setMPONum(intValueConvert(MPONum));
            queryDigitalTwinLinkParam.setLCNum(intValueConvert(LCNum));
            queryDigitalTwinLinkParam.setUpNum(intValueConvert(upNum));
            queryDigitalTwinLinkParam.setDownNum(intValueConvert(downNum));
            queryDigitalTwinLinkParam.setLinkInfoList(allList);
        }
    }

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


    public void getWeatherInfoByCity(QueryDigitalTwinShowParam param) {
        //获取存储设备的基础数据
        String url = "https://restapi.amap.com/v3/weather/weatherInfo";
        String jsonText = ModelOKHttpUtils.builder().url(url)
                .addParam("key", apiKey)
                .addParam("city", adcode)
                .get()
                .sync();
//        String jsonText = "{\"status\":\"1\",\"count\":\"1\",\"info\":\"OK\",\"infocode\":\"10000\",\"lives\":[{\"province\":\"四川\",\"city\":\"简阳市\",\"adcode\":\"510185\",\"weather\":\"晴\",\"temperature\":\"27\",\"winddirection\":\"西南\",\"windpower\":\"≤3\",\"humidity\":\"70\",\"reporttime\":\"2023-08-23 17:02:18\",\"temperature_float\":\"27.0\",\"humidity_float\":\"70.0\"}]}";
        JSONObject strInfoJson = JSONObject.parseObject(jsonText != null ? jsonText : "");
        //接口返回成功
        if (strInfoJson.get("status") != null && HTTP_SUCCESS.equals(strInfoJson.get("status").toString())) {
            List<Map> listMap = (List<Map>) strInfoJson.get("lives");
            if (CollectionUtils.isNotEmpty(listMap)) {
                Map m = listMap.get(0);
                String weather = strValueConvert(m.get("weather"));
                String temperature = strValueConvert(m.get("temperature"));
                param.setWeather(weather);
                param.setTemp(temperature);
            }
        }
    }

    /**
     * 资产设备数据参数转换
     *
     * @param deviceList
     * @return
     */
    private List<MwDigitalTwinItemParam> getDeviceAssetsIdInfo(List<MwModelAlertShowDeviceParam> deviceList) {
        List<MwDigitalTwinItemParam> list = new ArrayList<>();
        List<MwModelAlertShowDeviceParam> disList = deviceList.stream().filter(s -> !Strings.isNullOrEmpty(s.getAssetsId())
                && intValueConvert(s.getMonitorServerId()) != 0).collect(Collectors.toList());
        if (disList != null && disList.size() > 0) {
            List<String> collect = disList.stream().map(s -> strValueConvert(s.getId())).collect(Collectors.toList());
            Map<Integer, List<String>> groupMap = disList.stream()
                    .collect(Collectors.groupingBy(s -> intValueConvert(s.getMonitorServerId()), Collectors.mapping(s -> s.getAssetsId(), Collectors.toList())));
            groupMap.forEach((k, v) -> {
                MwDigitalTwinItemParam param = new MwDigitalTwinItemParam();
                param.setServerId(k);
                param.setHostIds(v);
                param.setAssetsIds(collect);
                list.add(param);
            });
        }
        return list;
    }


    private List<MwModelAlertShowRoomParam> getRoomInfoByBuildId(String id) {
        QueryEsParam queryEsParam = new QueryEsParam();
        QueryModelInstanceByPropertyIndexParam qParam = new QueryModelInstanceByPropertyIndexParam();
        //查询楼宇下的机房信息
        if (Strings.isNullOrEmpty(id)) {
            queryEsParam.setExistsList(Arrays.asList(RELATIONSITEFLOOR.getField()));
        } else {
            qParam.setPropertiesIndexId(RELATIONSITEFLOOR.getField());
            qParam.setPropertiesValueList(Arrays.asList(id));
        }
        queryEsParam.setParamLists(Arrays.asList(qParam));
        List<Map<String, Object>> roomListmap = mwModelCommonService.getAllInstanceInfoByQueryParam(queryEsParam);
        MwModelAlertShowRoomParam param = new MwModelAlertShowRoomParam();
        List<MwModelAlertShowRoomParam> list = new ArrayList<>();
        //所有机房Ids
        Set<Integer> roomIds = new HashSet<>();
        for (Map<String, Object> m : roomListmap) {
            param.setId(strValueConvert(m.get(INSTANCE_ID_KEY)));
            roomIds.add(intValueConvert(m.get(INSTANCE_ID_KEY)));
            param.setName(strValueConvert(m.get(INSTANCE_NAME_KEY)));
            param.setBuildId(strValueConvert(m.get(RELATIONSITEFLOOR.getField())));
            list.add(param);
        }
        List<MwModelAlertShowRoomParam> allRoomList = new ArrayList<>();
        //查询机房下的机柜设备
        qParam.setPropertiesIndexId(RELATIONSITEROOM.getField());
        qParam.setPropertiesValueList(new ArrayList(roomIds));
        queryEsParam.setParamLists(Arrays.asList(qParam));
        List<Map<String, Object>> cabinetListMap = mwModelCommonService.getAllInstanceInfoByQueryParam(queryEsParam);
        //组装机柜列表数据
        List<MwModelAlertShowCabinetParam> cabinetParamList = getCabinetListInfo(cabinetListMap);
        Map<String, List<MwModelAlertShowCabinetParam>> cabinetMap = cabinetParamList.stream().collect(Collectors.groupingBy(s -> s.getRoomId()));
        for (MwModelAlertShowRoomParam room : list) {
            MwModelAlertShowRoomParam roomParam = new MwModelAlertShowRoomParam();
            if (cabinetMap != null && cabinetMap.containsKey(room.getId())) {
                roomParam.setCabinets(cabinetMap.get(room.getId()));
            }
            roomParam.setName(room.getName());
            roomParam.setId(room.getId());
            allRoomList.add(roomParam);
        }

        return allRoomList;
    }

    private List<String> getRoomInfoAndCabinetIdsById(List<Integer> roomIds, QueryDigitalTwinShowParam showParam) {
        QueryEsParam queryEsParam = new QueryEsParam();
        List<QueryModelInstanceByPropertyIndexParam> paramLists = new ArrayList<>();
        QueryModelInstanceByPropertyIndexParam qParam = new QueryModelInstanceByPropertyIndexParam();
        MwModelInstanceCommonParam param = new MwModelInstanceCommonParam();
        param.setInstanceIdList(roomIds);
        List<MwModelInstanceCommonParam> allRoomInfo = mwModelInstanceDao.getAllRoomInfo(param);

        //查询mysql的所有的机柜数据
        List<MwModelInstanceCommonParam> allCabinetInfo = mwModelInstanceDao.getAllCabinetInfo();
        List<Integer> cabinetIdList = allCabinetInfo.stream().map(s -> s.getModelInstanceId()).collect(Collectors.toList());
        //查询机房下的机柜设备
        qParam.setPropertiesIndexId(RELATIONSITEROOM.getField());
        qParam.setPropertiesValueList(roomIds);
        paramLists.add(qParam);
        qParam = new QueryModelInstanceByPropertyIndexParam();
        qParam.setPropertiesIndexId(INSTANCE_ID_KEY);
        qParam.setPropertiesValueList(cabinetIdList);
        paramLists.add(qParam);
        queryEsParam.setParamLists(paramLists);
        List<Map<String, Object>> cabinetListMap = mwModelCommonService.getAllInstanceInfoByQueryParam(queryEsParam);
        Map<String, List<Map<String, Object>>> cabinetMaps = cabinetListMap.stream().collect(Collectors.groupingBy(s -> strValueConvert(s.get(RELATIONSITEROOM.getField()))));
        Set<String> cabinetIds = cabinetListMap.stream().map(s -> strValueConvert(s.get(INSTANCE_ID_KEY))).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(cabinetListMap)) {
            //机柜下总U位数
            int allUNum = 0;
            //已使用U位数
            int usedNum = 0;
            //机柜总数量
            int allCabinetNum = cabinetListMap.size();
            //已使用机柜数量
            int usedCabinetNum = 0;
            for (Map<String, Object> m : cabinetListMap) {
                List<CabinetLayoutDataParam> cabinetLayoutInfo = JSONArray.parseArray(JSONArray.toJSONString(m.get(LAYOUTDATA.getField())), CabinetLayoutDataParam.class);
                allUNum += intValueConvert(m.get(UNUM.getField()));
                usedNum += getUsedUNum(cabinetLayoutInfo);
                if (usedNum > 0) {
                    usedCabinetNum++;
                }
            }
            //机柜下总U位数
            showParam.setAllUNum(allUNum);
            //已使用U位数
            showParam.setUsedUNum(usedNum);
            //机柜总数量
            showParam.setCabinetNum(allCabinetNum);
            //已使用机柜数量
            showParam.setCabinetUsed(usedCabinetNum);
        }
        int usedRoom = 0;
        for (MwModelInstanceCommonParam room : allRoomInfo) {
            if (cabinetMaps != null && cabinetMaps.containsKey(strValueConvert(room.getModelInstanceId()))) {
                usedRoom++;
            }
            roomName = room.getModelInstanceName();
        }
        showParam.setRoomNum(allRoomInfo.size());
        showParam.setRoomUsed(usedRoom);
        return new ArrayList<>(cabinetIds);
    }

    private List<MwModelAlertShowRoomParam> getRoomInfo(List<Integer> roomIds) {
        QueryEsParam queryEsParam = new QueryEsParam();
        List<MwModelAlertShowRoomParam> list = new ArrayList<>();
        QueryModelInstanceByPropertyIndexParam qParam = new QueryModelInstanceByPropertyIndexParam();
        MwModelInstanceCommonParam param = new MwModelInstanceCommonParam();
        param.setInstanceIdList(roomIds);
        List<MwModelInstanceCommonParam> allRoomInfo = mwModelInstanceDao.getAllRoomInfo(param);
        Set<Integer> instanceIds = allRoomInfo.stream().map(s -> s.getModelInstanceId()).collect(Collectors.toSet());
        //查询机房下的机柜设备
        qParam.setPropertiesIndexId(RELATIONSITEROOM.getField());
        qParam.setPropertiesValueList(new ArrayList(instanceIds));
        queryEsParam.setParamLists(Arrays.asList(qParam));
        List<Map<String, Object>> cabinetListMap = mwModelCommonService.getAllInstanceInfoByQueryParam(queryEsParam);
        //组装机柜列表数据
        List<MwModelAlertShowCabinetParam> cabinetParamList = getCabinetListInfo(cabinetListMap);
        Map<String, List<MwModelAlertShowCabinetParam>> cabinetMap = cabinetParamList.stream().collect(Collectors.groupingBy(s -> s.getRoomId()));
        for (MwModelInstanceCommonParam room : allRoomInfo) {
            MwModelAlertShowRoomParam roomParam = new MwModelAlertShowRoomParam();
            if (cabinetMap != null && cabinetMap.containsKey(strValueConvert(room.getModelInstanceId()))) {
                roomParam.setCabinets(cabinetMap.get(strValueConvert(room.getModelInstanceId())));
            }
            roomParam.setName(room.getModelInstanceName());
            roomParam.setId(strValueConvert(room.getModelInstanceId()));
            list.add(roomParam);
        }
        return list;
    }


    private int getUsedUNum(List<CabinetLayoutDataParam> cabinetLayoutInfo) {
        if (CollectionUtils.isEmpty(cabinetLayoutInfo)) {
            return 0;
        }
        int usedNum = 0;
        for (CabinetLayoutDataParam layoutDataParam : cabinetLayoutInfo) {
            if (layoutDataParam.getIsUsed() != null && layoutDataParam.getIsUsed()) {
                Integer startIndex = intValueConvert(layoutDataParam.getStart());
                Integer endIndex = intValueConvert(layoutDataParam.getEnd());
                usedNum += (endIndex - startIndex + 1);
            }

        }
        return usedNum;
    }


    private String getModelTypeById(String id) {
        String type = "building";
        if (Strings.isNullOrEmpty(id)) {
            return type;
        }
        try {
            type = mwModelInstanceDao.getModelTypeById(id);
        } catch (Exception e) {
            log.error("getModelTypeById to fail :", e);
        }
        return type;
    }


    /**
     * 获取楼宇信息并返回机房ids
     *
     * @param id 楼宇Id
     * @return
     */
    private List<Integer> getBuildInfoAndRoomIdsById(String id, QueryDigitalTwinShowParam showParam) throws Exception {
        List<Map<String, Object>> floorList = mwModelInstanceService.selectInfosByModelId(floorModelId);
        String buildLocation = BUILDINGADDRESS;
        for(Map<String, Object> map : floorList){
           String location = strValueConvert(map.get(BUILD_LOCATION));
            if(!Strings.isNullOrEmpty(buildLocation)){
                buildLocation = location;
            }
        }
        MwModelInstanceCommonParam param = new MwModelInstanceCommonParam();
        if (!Strings.isNullOrEmpty(id)) {
            //指定楼宇Id，查询机房信息
            param.setRelationInstanceId(intValueConvert(id));
        }
        List<MwModelInstanceCommonParam> allRoomInfo = mwModelInstanceDao.getAllRoomInfo(param);
        showParam.setAddress(buildLocation); //楼宇地址信息
        Set<Integer> roomIds = new HashSet<>();
        for (MwModelInstanceCommonParam m : allRoomInfo) {
            roomIds.add(m.getModelInstanceId());
        }


//        QueryEsParam queryEsParam = new QueryEsParam();
//        QueryModelInstanceByPropertyIndexParam qParam = new QueryModelInstanceByPropertyIndexParam();
//        //查询楼宇下的机房信息
//        long time1 = System.currentTimeMillis();
//        if (Strings.isNullOrEmpty(id)) {
//            //查询所有关联所属楼宇字段的数据
//            queryEsParam.setExistsList(Arrays.asList(RELATION_FLOOR));
//        } else {
//            qParam.setPropertiesIndexId(RELATION_FLOOR);
//            qParam.setPropertiesValueList(Arrays.asList(id));
////            queryEsParam.setModelIndexs(Arrays.asList("mw_20ec5003b3e448c1b7df79086fdec0b5"));
//        }
//
//        queryEsParam.setParamLists(Arrays.asList(qParam));
//        List<Map<String, Object>> roomListmap = mwModelCommonService.getAllInstanceInfoByQueryParam(queryEsParam);
//        long time2 = System.currentTimeMillis();
//        log.info("查询楼宇耗时::"+(time2-time1)+"ms");
        //所有机房Ids
//        Set<String> roomIds = new HashSet<>();
//        for (Map<String, Object> m : roomListmap) {
//            roomIds.add(strValueConvert(m.get(INSTANCE_ID_KEY)));
//        }
//        if (Strings.isNullOrEmpty(id)) {
//
//        } else {
//            //查询楼宇信息
//            qParam = new QueryModelInstanceByPropertyIndexParam();
//            qParam.setPropertiesIndexId(INSTANCE_ID_KEY);
//            qParam.setPropertiesValueList(Arrays.asList(id));
//            queryEsParam = new QueryEsParam();
//            queryEsParam.setParamLists(Arrays.asList(qParam));
//            List<Map<String, Object>> buildList = mwModelCommonService.getAllInstanceInfoByQueryParam(queryEsParam);
//            MwModelAlertShowBuildParam buildParam = new MwModelAlertShowBuildParam();
//            for (Map<String, Object> m : buildList) {
//                showParam.setAddress(strValueConvert(m.get("buildLocation"))); //楼宇地址信息
////                buildName = strValueConvert(m.get(INSTANCE_NAME_KEY));
//            }
//        }
        return new ArrayList(roomIds);
    }


    /**
     * 根据Id获取数据
     *
     * @param Id 楼宇Id
     * @return
     */
    private MwModelAlertShowBuildParam getBuildInfoById(String Id) {
        QueryEsParam queryEsParam = new QueryEsParam();
        QueryModelInstanceByPropertyIndexParam qParam = new QueryModelInstanceByPropertyIndexParam();
        //查询资产设备
        qParam.setPropertiesIndexId(INSTANCE_ID_KEY);
        qParam.setPropertiesValueList(Arrays.asList(Id));
        queryEsParam.setParamLists(Arrays.asList(qParam));
        List<Map<String, Object>> buildList = mwModelCommonService.getAllInstanceInfoByQueryParam(queryEsParam);
        MwModelAlertShowBuildParam buildParam = new MwModelAlertShowBuildParam();
        for (Map<String, Object> m : buildList) {
            buildParam.setAddress(strValueConvert(m.get("buildLocation"))); //楼宇地址信息
            buildParam.setName(strValueConvert(m.get(INSTANCE_NAME_KEY)));
        }
        return buildParam;
    }


    /**
     * 根据资产Id获取资产数据
     *
     * @param deviceId 资产Id
     * @return
     */
    private List<MwModelAlertShowDeviceParam> getDeviceInfoById(String deviceId) {
        QueryEsParam queryEsParam = new QueryEsParam();
        QueryModelInstanceByPropertyIndexParam qParam = new QueryModelInstanceByPropertyIndexParam();
        //查询机柜下的资产设备
        qParam.setPropertiesIndexId(INSTANCE_ID_KEY);
        qParam.setPropertiesValueList(Arrays.asList(deviceId));
        queryEsParam.setParamLists(Arrays.asList(qParam));
        List<Map<String, Object>> deviceList = mwModelCommonService.getAllInstanceInfoByQueryParam(queryEsParam);
        //获取设备列表信息
        List<MwModelAlertShowDeviceParam> deviceParamList = getDeviceListInfo(deviceList);
        return deviceParamList;
    }

    /**
     * 根据机柜Id列表获取所有资产数据
     *
     * @param cabinetIds 机柜Ids
     * @return
     */
    private List<MwModelAlertShowDeviceParam> getDeviceListByCabinetIds(List<String> cabinetIds) {
        long time1 = System.currentTimeMillis();
        QueryEsParam queryEsParam = new QueryEsParam();
        List<QueryModelInstanceByPropertyIndexParam> paramLists = new ArrayList<>();
        QueryModelInstanceByPropertyIndexParam qParam = new QueryModelInstanceByPropertyIndexParam();
        //基础设备下的所有实例id
        List<ModelInstanceBaseInfoDTO> baseInstanceInfoList = mwModelViewDao.getModelIndexANDInstanceInfo(true);
        List<Integer> baseInstanceIds = baseInstanceInfoList.stream().map(s -> s.getInstanceId()).collect(Collectors.toList());
        //查询机柜下的资产设备
        qParam.setPropertiesIndexId(RELATIONSITECABINET.getField());
        qParam.setPropertiesValueList(cabinetIds);
        paramLists.add(qParam);
        qParam = new QueryModelInstanceByPropertyIndexParam();
        qParam.setPropertiesIndexId(INSTANCE_ID_KEY);
        qParam.setPropertiesValueList(baseInstanceIds);
        paramLists.add(qParam);
        queryEsParam.setParamLists(paramLists);
        List<Map<String, Object>> deviceList = mwModelCommonService.getAllInstanceInfoByQueryParam(queryEsParam);
//        List<Map<String, Object>> newList = mwModelViewServiceImpl.getAssetsStateByZabbix(deviceList);
        //获取设备列表信息
        List<MwModelAlertShowDeviceParam> deviceParamList = getDeviceListInfo(deviceList);
        long time2 = System.currentTimeMillis();
        return deviceParamList;
    }

    private void getDigitalTwinShowByDevice(List<MwModelAlertShowDeviceParam> deviceParamList, QueryDigitalTwinShowParam showParam) {
        long manageNum = 0;
        long typeNum = 0;
        long netWorkNum = 0;
        long serverNum = 0;
        long storageNum = 0;
        if (CollectionUtils.isNotEmpty(deviceParamList)) {
            //纳管设备
            manageNum += deviceParamList.stream().filter(s -> (intValueConvert(s.getMonitorServerId()) != 0 && intValueConvert(s.getAssetsId()) != 0)).count();
            //设备类型种类数量
            typeNum += deviceParamList.stream().filter(s -> !Strings.isNullOrEmpty(s.getType())).map(MwModelAlertShowDeviceParam::getType).distinct().count();
            //网络设备数量
            netWorkNum += deviceParamList.stream().filter(s -> intValueConvert(s.getTypeId()) == 2).count();
            //服务器数量
            serverNum += deviceParamList.stream().filter(s -> intValueConvert(s.getTypeId()) == 1 || intValueConvert(s.getTypeId()) == 69).count();
            //存储设备数量
            storageNum += deviceParamList.stream().filter(s -> intValueConvert(s.getTypeId()) == 4).count();;

            Set<String> typeList = deviceParamList.stream().filter(s -> !Strings.isNullOrEmpty(s.getType())).map(MwModelAlertShowDeviceParam::getType).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(typeList)) {
                showParam.setDeviceName(String.join(",", typeList));
            }
        }
        showParam.setManageDeviceNum(intValueConvert(manageNum));
        showParam.setDeviceTypeNum(intValueConvert(typeNum));
        showParam.setNetWorkDeviceNum(intValueConvert(netWorkNum));
        showParam.setServerDeviceNum(intValueConvert(serverNum));
        showParam.setStorageDeviceNum(intValueConvert(storageNum));
    }

    /**
     * 机柜信息
     *
     * @param ids 机柜Ids
     * @return
     */
    private void getCabinetInfo(List<String> ids, QueryDigitalTwinShowParam showParam) {
        QueryEsParam queryEsParam = new QueryEsParam();
        QueryModelInstanceByPropertyIndexParam qParam = new QueryModelInstanceByPropertyIndexParam();
        //查询机柜下的资产设备
        qParam.setPropertiesIndexId(INSTANCE_ID_KEY);
        qParam.setPropertiesValueList(ids);
        queryEsParam.setParamLists(Arrays.asList(qParam));
        List<Map<String, Object>> cabinetListMap = mwModelCommonService.getAllInstanceInfoByQueryParam(queryEsParam);
        List<MwModelAlertShowCabinetParam> cabinetParamList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(cabinetListMap)) {
            //机柜下总U位数
            int allUNum = 0;
            //已使用U位数
            int usedNum = 0;
            //机柜总数量
            int allCabinetNum = cabinetListMap.size();
            //已使用机柜数量
            int usedCabinetNum = 0;
            for (Map<String, Object> m : cabinetListMap) {
                List<CabinetLayoutDataParam> cabinetLayoutInfo = JSONArray.parseArray(JSONArray.toJSONString(m.get(LAYOUTDATA.getField())), CabinetLayoutDataParam.class);
                allUNum += intValueConvert(m.get(UNUM.getField()));
                usedNum += getUsedUNum(cabinetLayoutInfo);
                if (usedNum > 0) {
                    usedCabinetNum++;
                }
            }
            //机柜下总U位数
            showParam.setAllUNum(allUNum);
            //已使用U位数
            showParam.setUsedUNum(usedNum);
            //机柜总数量
            showParam.setCabinetNum(allCabinetNum);
            //已使用机柜数量
            showParam.setCabinetUsed(usedCabinetNum);
        }
    }


    /**
     * 机柜数据组装
     *
     * @param cabinetListMap
     * @return
     */
    private List<MwModelAlertShowCabinetParam> getCabinetListInfo(List<Map<String, Object>> cabinetListMap) {
        List<MwModelAlertShowCabinetParam> cabinetParamList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(cabinetListMap)) {
            for (Map<String, Object> m : cabinetListMap) {
                MwModelAlertShowCabinetParam cabinetParam = new MwModelAlertShowCabinetParam();
                List<CabinetLayoutDataParam> cabinetLayoutInfo = JSONArray.parseArray(JSONArray.toJSONString(m.get(LAYOUTDATA.getField())), CabinetLayoutDataParam.class);
                //机柜Id
                cabinetParam.setId(strValueConvert(m.get(INSTANCE_ID_KEY)));
                //机柜名称
                cabinetParam.setName(strValueConvert(m.get(INSTANCE_NAME_KEY)));
                cabinetParam.setUNum(intValueConvert(m.get(UNUM.getField())));
                cabinetParam.setRoomId(strValueConvert(m.get(RELATIONSITEROOM.getField())));
                cabinetParam.setLayoutData(cabinetLayoutInfo);
                cabinetParamList.add(cabinetParam);
            }
        }
        return cabinetParamList;
    }

    /**
     * 资产设备数据组装
     *
     * @param newList
     * @return
     */
    private List<MwModelAlertShowDeviceParam> getDeviceListInfo(List<Map<String, Object>> newList) {
        List<MwModelAlertShowDeviceParam> deviceParamList = new ArrayList<>();
        for (Map<String, Object> m : newList) {
            MwModelAlertShowDeviceParam deviceParam = new MwModelAlertShowDeviceParam();
            //设备Id
            deviceParam.setId(strValueConvert(m.get(INSTANCE_ID_KEY)));
            //设备名称
            deviceParam.setName(strValueConvert(m.get(INSTANCE_NAME_KEY)));
            deviceParam.setType(strValueConvert(m.get(ASSETSUBTYPE_NAME)));
            deviceParam.setTypeId(intValueConvert(m.get(ASSETTYPE_ID_KEY)));
            deviceParam.setMonitorServerId(intValueConvert(m.get(MONITOR_SERVER_ID)));
            deviceParam.setAssetsId(strValueConvert(m.get(ASSETS_ID)));
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
            deviceParamList.add(deviceParam);
        }
        return deviceParamList;
    }


    private String getCurrentLocaltion(String type) {
        String currentLocaltion = "";
        if (ROOM.equals(type)) {
            currentLocaltion = buildName + " > " + roomName;
        } else if (CABINET.equals(type)) {
            currentLocaltion = buildName + " > " + roomName + " > " + cabinetName;
        } else if (LOOKLINK.equals(type)) {
            currentLocaltion = buildName + " > " + roomName + " > " + cabinetName + " > " + lookLinkName;
        }else if (DEVICE.equals(type)) {
            currentLocaltion = buildName + " > " + roomName + " > " + cabinetName + " > " + deviceUNum;
        } else if (LINK.equals(type)) {
            currentLocaltion = buildName + " > " + roomName + " > " + cabinetName + " > " + deviceUNum + " > " + linkName;
        }
        return currentLocaltion;
    }
}
