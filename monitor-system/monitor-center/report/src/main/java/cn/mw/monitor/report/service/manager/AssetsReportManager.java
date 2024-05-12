package cn.mw.monitor.report.service.manager;

import cn.mw.monitor.report.dto.TrendParam;
import cn.mw.monitor.report.dto.assetsdto.AssetsDto;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.label.api.MwLabelCommonServcie;
import cn.mw.monitor.service.user.api.MWCommonService;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.state.DataType;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author xhy
 * @date 2020/12/29 14:45
 */
@Component
@Slf4j
public class AssetsReportManager {
    private static final Logger logger = LoggerFactory.getLogger("cn/mw/monitor/AssetsReportManager");

    @Autowired
    private MWCommonService mwCommonService;

    @Autowired
    private MwLabelCommonServcie mwLabelCommonServcie;

    public static HashMap<String, AssetsLabel> labelMap = new HashMap<>();
    public static List<String> labelIds = new ArrayList<>();

    static {
        for (AssetsLabel assetsLabel : AssetsLabel.values()) {
            labelMap.put(assetsLabel.getId(), assetsLabel);
            labelIds.add(assetsLabel.getId());
        }
        Collections.sort(labelIds);
    }


    public List<AssetsDto> getReport(TrendParam trendParam) {
        List<AssetsDto> list = new ArrayList<>();
        //查询含有一级数据采集标签的资产
        //1查询所有网络设备的资产id
        List<MwTangibleassetsTable> mwTangibleassetsDTOS = trendParam.getMwTangibleassetsDTOS();
        if (mwTangibleassetsDTOS.size() > 0) {
            ThreadPoolExecutor executorService = new ThreadPoolExecutor(20, 50, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
            List<Future<AssetsDto>> futures = new ArrayList<>();
            mwTangibleassetsDTOS.forEach(mwTangibleassetsTable -> {
                Callable<AssetsDto> callable = new Callable<AssetsDto>() {
                    @Override
                    public AssetsDto call() throws Exception {
                        String id = mwTangibleassetsTable.getId();
                        List<String> listLabelIds = mwLabelCommonServcie.getLabelIdsByAssetsId(id, DataType.ASSETS.getName());
                        Collections.sort(listLabelIds);
                        AssetsDto assetsDto = new AssetsDto();
//                        assetsDto.setId(id);
                        assetsDto.setAssetsName(mwTangibleassetsTable.getAssetsName());
                        assetsDto.setAssertIp(mwTangibleassetsTable.getInBandIp());
                        assetsDto.setAssetsTypeName(mwTangibleassetsTable.getAssetsTypeName());
                        assetsDto.setDescription(mwTangibleassetsTable.getDescription());
                        assetsDto.setModificationDate(MWUtils.getDate(mwTangibleassetsTable.getModificationDate(), "yyyy-MM-dd HH:mm:ss"));
                        assetsDto.setManufacturer(mwTangibleassetsTable.getManufacturer());
                        assetsDto.setSpecifications(mwTangibleassetsTable.getSpecifications());
                        assetsDto.setCategory(mwTangibleassetsTable.getAssetsTypeSubName());
                        assetsDto.setUseState(mwTangibleassetsTable.getMonitorFlag()==true?"启用":"未启用");
                        //assetsDto.setSystemVersion(mwTangibleassetsTable.getDescription());
                        List<String> orgNames = mwCommonService.getOrgNameByTypeId(id, DataType.ASSETS.getName());
                        assetsDto.setOrgName(orgNames.toString());
//                        if (labelIds.toString().equals(listLabelIds.toString())) {
//                            //查询资产对应的标签值
//                            List<Map<String, String>> mapList = mwLabelCommonServcie.getLabelsByAssetsId(id, DataType.ASSETS.getName());
//                            mapList.forEach(map -> {
//                                String labelId = String.valueOf(map.get("labelId"));
//                                AssetsLabel assetsLabel = labelMap.get(labelId);
//                                String value = map.get("value");
//                                switch (assetsLabel) {
//                                    case category:
//                                        assetsDto.setCategory(value);
//                                        break;
//                                    case useDate:
//                                        assetsDto.setUseDate(value);
//                                        break;
//                                    case useState:
//                                        assetsDto.setUseState(value);
//                                        break;
//                                    case assetsCode:
//                                        assetsDto.setAssetsCode(value);
//                                        break;
//                                    case brandLand:
//                                        assetsDto.setBrandLand(value);
//                                        break;
//                                    case systemVersion:
//                                        assetsDto.setSystemVersion(value);
//                                        break;
//                                    case deviceHeight:
//                                        assetsDto.setDeviceHeight(value);
//                                        break;
//                                    case supportIpv6:
//                                        assetsDto.setSupportIpv6(value);
//                                        break;
//                                    case influenceSystem:
//                                        assetsDto.setInfluenceSystem(value);
//                                        break;
//                                    case deployArea:
//                                        assetsDto.setDeployArea(value);
//                                        break;
//                                    case operationDepartment:
//                                        assetsDto.setOperationDepartment(value);
//                                        break;
//                                    case belongCabinet:
//                                        assetsDto.setBelongCabinet(value);
//                                        break;
//                                    case slotNo:
//                                        assetsDto.setSlotNo(value);
//                                        break;
//                                    default:
//                                        break;
//                                }
//                            });
//                        }
                        return assetsDto;
                    }
                };
                Future<AssetsDto> submit = executorService.submit(callable);
                futures.add(submit);

            });
            futures.forEach(f -> {
                try {
                    AssetsDto assetsDto = f.get(10, TimeUnit.MINUTES);
                    list.add(assetsDto);
                } catch (Exception e) {
                    logger.error("{getReport{}}", e);
                }
            });
        }
        return list;

    }

}
