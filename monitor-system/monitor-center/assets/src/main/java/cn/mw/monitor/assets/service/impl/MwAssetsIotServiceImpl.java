package cn.mw.monitor.assets.service.impl;

import cn.mw.monitor.assets.api.param.assets.IotParam;
import cn.mw.monitor.assets.api.param.assets.IotTypeParam;
import cn.mw.monitor.state.DataType;
import cn.mw.monitor.user.dto.GlobalUserInfo;
import cn.mw.monitor.user.service.MWUserService;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import cn.mw.monitor.common.constant.ErrorConstant;
import cn.mw.monitor.api.common.UuidUtil;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.assets.api.param.assets.AddUpdateAssetsIotParam;
import cn.mw.monitor.assets.dao.MwAssetsIotDao;
import cn.mw.monitor.assets.dto.AssetsIotDto;
import cn.mw.monitor.assets.dto.MwAssetsIotListDto;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.service.assets.model.MwCommonAssetsDto;
import cn.mw.monitor.assets.dto.SoundParam;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.assets.service.MwAssetsIotService;
import cn.mw.monitor.assets.service.thread.GetAssetsIotListThread;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author xhy
 * @date 2020/6/6 10:37
 */
@Service
@Slf4j
public class MwAssetsIotServiceImpl implements MwAssetsIotService {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/assetsIot");

    @Resource
    private MwAssetsIotDao assetsIotDao;

    @Autowired
    private ILoginCacheInfo iLoginCacheInfo;

    @Resource
    private MwAssetsManager mwAssetsManager;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private MWUserService userService;

    @Override
    public Reply aupdate(AddUpdateAssetsIotParam auParam) {
        try {
            auParam.setModifier(iLoginCacheInfo.getLoginName());
            int count = assetsIotDao.selectAssetsIotId(auParam.getAssetsId());
            if (count != 0) {
                assetsIotDao.updateAssetsIot(auParam);
            } else {
                auParam.setCreator(iLoginCacheInfo.getLoginName());
                auParam.setId(UuidUtil.getUid());
                assetsIotDao.addAssetsIot(auParam);
            }
            logger.info("ACCESS_IOT_LOG[]AssetsIOT[]温湿度资产管理[]用户修改触发告警的温湿度阈值[]{}", auParam);
            return Reply.ok("编辑成功");

        } catch (Exception e) {
            logger.error("fail to updateAssets with AddUpdateAssetsIotParam={}, cause:{}", auParam, e);
            return Reply.fail(ErrorConstant.ASSETS_IOT_UPDATE_CODE_305001, ErrorConstant.ASSETS_IOT_UPDATE_MSG_305001);
        }
    }


    @Override
    public Reply selectList(AddUpdateAssetsIotParam param) {
        try {
            logger.info("进入查询");
            String loginName = iLoginCacheInfo.getLoginName();
            Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
            MwCommonAssetsDto mwCommonAssetsDto = MwCommonAssetsDto.builder().userId(userId).assetsTypeId(30).build();
            if (null != param && null != param.getAssetsId() && StringUtils.isNotEmpty(param.getAssetsId())) {
                mwCommonAssetsDto.setAssetsId(param.getAssetsId());
            }
            Map<String, Object> assetsByUserId = mwAssetsManager.getAssetsByUserId(mwCommonAssetsDto);
            Object assets = assetsByUserId.get("assetsList");
            List<MwTangibleassetsTable> assetsList = new ArrayList<>();
            List<MwAssetsIotListDto> list = new ArrayList<>();
            if (null != assets) {
                assetsList = (List<MwTangibleassetsTable>) assets;
            }
            if (assetsList.size() > 0) {
                int threadSize = assetsList.size() > 1 ? (assetsList.size() / 2) : assetsList.size();
                ExecutorService executorService = Executors.newFixedThreadPool(threadSize);
                List<Future<MwAssetsIotListDto>> futureList = new ArrayList<>();
                assetsList.forEach(assetsData -> {
                    String assetId = assetsData.getAssetsId();
                    GetAssetsIotListThread getAssetsIotListThread = new GetAssetsIotListThread() {
                        @Override
                        public MwAssetsIotListDto call() throws Exception {
                            MWZabbixAPIResult result = mwtpServerAPI.itemGetbyType(assetsData.getMonitorServerId(), "MW_IOT", assetId, false);
                            MwAssetsIotListDto assetsIotListDto = new MwAssetsIotListDto();
                            assetsIotListDto.setDevStatus("ACTIVE");
                            assetsIotListDto.setAssetsName(assetsData.getAssetsName());
                            assetsIotListDto.setAssetsId(assetsData.getId());
                            assetsIotListDto.setHostId(assetsData.getAssetsId());
                            assetsIotListDto.setMonitorServerId(assetsData.getMonitorServerId());
                            AssetsIotDto assetsIotDto = assetsIotDao.selectAssetsIot(assetId);
                            if (null != assetsIotDto) {
                                assetsIotListDto.setVoice(assetsIotDto.getVoice());
                            } else {
                                assetsIotListDto.setVoice(false);
                            }
                            if (result.getCode() == 0) {
                                JsonNode jsonNode = (JsonNode) result.getData();
                                if (jsonNode.size() > 0) {
                                    jsonNode.forEach(data -> {
                                        String name = data.get("name").asText();
                                        if (name.equals("MW_IOT_HUMIDITY_VALUE")) {
                                           /* long clock = data.get("lastclock").asLong();
                                            String date = SeverityUtils.getDate(clock);
                                            assetsIotListDto.setUpdateTime(date);*/
                                            Double lastvalue = data.get("lastvalue").asDouble();
                                            assetsIotListDto.setHum(lastvalue);
                                            if (null != assetsIotDto) {
                                                assetsIotListDto.setVoice(assetsIotDto.getVoice());
                                                String humCondition = assetsIotDto.getHumCondition();
                                                if (null != humCondition) {
                                                    switch (humCondition) {
                                                        case "大于":
                                                            if (lastvalue > assetsIotDto.getHumThreshold()) {
                                                                assetsIotListDto.setHumAlarm(1);
                                                            } else {
                                                                assetsIotListDto.setHumAlarm(0);
                                                            }
                                                            break;
                                                        case "小于":
                                                            if (lastvalue < assetsIotDto.getHumThreshold()) {
                                                                assetsIotListDto.setHumAlarm(1);
                                                            } else {
                                                                assetsIotListDto.setHumAlarm(0);
                                                            }
                                                            break;
                                                        case "等于":
                                                            if (lastvalue == assetsIotDto.getHumThreshold()) {
                                                                assetsIotListDto.setHumAlarm(1);
                                                            } else {
                                                                assetsIotListDto.setHumAlarm(0);
                                                            }
                                                            break;
                                                        default:
                                                            break;
                                                    }
                                                }
                                            }
                                        } else if (name.equals("MW_IOT_TEMPERTURE_VALUE")) {
                                            Double lastvalue = data.get("lastvalue").asDouble();
                                            logger.info("查询温度{}", lastvalue);
                                            assetsIotListDto.setTem(lastvalue);
                                            if (null != assetsIotDto) {
                                                String temCondition = assetsIotDto.getTemCondition();
                                                if (null != temCondition) {
                                                    switch (temCondition) {
                                                        case "大于":
                                                            if (lastvalue > assetsIotDto.getTemThreshold()) {
                                                                assetsIotListDto.setTemAlarm(1);
                                                            } else {
                                                                assetsIotListDto.setTemAlarm(0);
                                                            }
                                                            break;
                                                        case "小于":
                                                            if (lastvalue < assetsIotDto.getTemThreshold()) {
                                                                assetsIotListDto.setTemAlarm(1);
                                                            } else {
                                                                assetsIotListDto.setTemAlarm(0);
                                                            }
                                                            break;
                                                        case "等于":
                                                            if (lastvalue == assetsIotDto.getTemThreshold()) {
                                                                assetsIotListDto.setTemAlarm(1);
                                                            } else {
                                                                assetsIotListDto.setTemAlarm(0);
                                                            }
                                                            break;
                                                        default:
                                                            break;
                                                    }
                                                }
                                            }

                                        }
                                    });
                                }
                            }
                            if (null != assetsIotListDto.getTemAlarm() && null != assetsIotListDto.getHumAlarm()) {
                                if (assetsIotListDto.getTemAlarm() == 1 && assetsIotListDto.getHumAlarm() == 1) {
                                    assetsIotListDto.setAlarmState("allAlarm");
                                } else if (assetsIotListDto.getTemAlarm() == 1 && assetsIotListDto.getHumAlarm() == 0) {
                                    assetsIotListDto.setAlarmState("temAlarm");
                                } else if (assetsIotListDto.getTemAlarm() == 0 && assetsIotListDto.getHumAlarm() == 1) {
                                    assetsIotListDto.setAlarmState("humAlarm");
                                }
                            }
                            return assetsIotListDto;
                        }
                    };
                    Future<MwAssetsIotListDto> f = executorService.submit(getAssetsIotListThread);
                    futureList.add(f);
                });
                futureList.forEach(f -> {
                    try {
                        MwAssetsIotListDto assetsIotListDto = f.get(15, TimeUnit.SECONDS);
                        list.add(assetsIotListDto);
                    } catch (Exception e) {
                        f.cancel(true);
                    }
                });
                executorService.shutdown();
            }
            logger.info("ACCESS_IOT_LOG[]AssetsIOT[]温湿度资产管理[]用户查询触发告警的温湿度阈值告警列表[]{}", list);
            return Reply.ok(list);
        } catch (Exception e) {
            logger.error("fail to selectList , cause:{}", e.getMessage());
            return Reply.fail(ErrorConstant.ASSETS_IOT_BROWSE_CODE_305002, ErrorConstant.ASSETS_IOT_BROWSE_MSG_305002);
        }
    }

    @Override
    public Reply updateVoice(SoundParam soundParam) {
        try {
            soundParam.setModifier(iLoginCacheInfo.getLoginName());
            int count = assetsIotDao.selectAssetsIotId(soundParam.getAssetsId());
            if (count != 0) {
                assetsIotDao.updateVoice(soundParam);
            } else {
                soundParam.setCreator(iLoginCacheInfo.getLoginName());
                soundParam.setId(UuidUtil.getUid());
                assetsIotDao.insertVoice(soundParam);
            }
            logger.info("ACCESS_IOT_LOG[]AssetsIOT[]温湿度资产管理[]告警声音启动或关闭[]{}", soundParam);
            return Reply.ok("修改声音成功");
        } catch (Exception e) {
            logger.error("fail to updateVoice with soundParam={}, cause:{}", soundParam, e);
            return Reply.fail(ErrorConstant.ASSETS_IOT_UPDATE_VOICE_CODE_305003, ErrorConstant.ASSETS_IOT__UPDATE_VOICE_MSG_305003);
        }
    }

    @Override
    public Reply selectThreshold(String assetsId) {
        try {
            AssetsIotDto assetsIotDto = new AssetsIotDto();
            AssetsIotDto assetsIotDto1 = assetsIotDao.selectAssetsIot(assetsId);
            if (null != assetsIotDto1) {
                assetsIotDto = assetsIotDto1;
            }
            logger.info("ACCESS_IOT_LOG[]AssetsIOT[]温湿度资产管理[]查询阈值[]{}", assetsId);
            return Reply.ok(assetsIotDto);
        } catch (Exception e) {
            logger.error("fail to updateAssets with mtaDTO={}, cause:{}", assetsId, e);
            return Reply.fail(ErrorConstant.ASSETS_IOT_UPDATE_VOICE_CODE_305003, ErrorConstant.ASSETS_IOT__UPDATE_VOICE_MSG_305003);
        }
    }

    @Override
    public Reply selectIotTypeList() {
        List<IotParam> list = new ArrayList<>();
        IotParam allType = new IotParam();
        allType.setTypeName("全部分类");
        AtomicReference<Integer> allTypeCount = new AtomicReference<>(0);
        try {
            List<IotTypeParam> iotTypeList = getCurrLoginUserPerm();
            iotTypeList.forEach(item->{
                if (null==item.getAssetsCount()||"".equals(item)){
                    item.setAssetsCount("0");
                }
                allTypeCount.updateAndGet(v -> v + Integer.parseInt(item.getAssetsCount()));
            });
            allType.setAssetsCount(allTypeCount.toString());
            allType.setIotTypeParams(iotTypeList);
            allType.setTypeId(iotTypeList.get(0).getPid());
            list.add(allType);
            return Reply.ok(list);
        } catch (Exception e) {
            log.error("fail to getIotTypeList with cause:", e);
            return Reply.fail(ErrorConstant.ASSETS_IOT_BROWSE_TYPE_CODE_305000,ErrorConstant.ASSETS_IOT_BROWSE_TYPE_MSG_305000);
        }
    }


    /**
     * 获取当前登录用户权限并过滤资产
     */
    private List<IotTypeParam> getCurrLoginUserPerm(){
        String loginName = iLoginCacheInfo.getLoginName();
        Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
        GlobalUserInfo globalUser = userService.getGlobalUser(userId);
        if(globalUser.isSystemUser())return assetsIotDao.selectIotTypeList(null);
        List<String> assetsIds = userService.getAllTypeIdList(globalUser, DataType.ASSETS);
        if(CollectionUtils.isNotEmpty(assetsIds)){
            return assetsIotDao.selectIotTypeList(assetsIds);
        }
        List<IotTypeParam> iotTypeParams = assetsIotDao.selectIotTypeList(assetsIds);
        iotTypeParams.forEach(item->{
            item.setAssetsCount("0");
        });
        return iotTypeParams;
    }
}
