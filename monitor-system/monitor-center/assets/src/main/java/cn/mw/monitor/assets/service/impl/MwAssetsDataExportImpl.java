package cn.mw.monitor.assets.service.impl;

import cn.mw.monitor.assets.dao.MwAssetsDataExportDao;
import cn.mw.monitor.assets.dao.MwAssetsNewFieldDao;
import cn.mw.monitor.assets.dto.*;
import cn.mw.monitor.assets.service.MwAssetsDataExportService;
import cn.mw.monitor.assets.service.MwAssetsNewFieldService;
import cn.mw.monitor.assets.utils.AttributesExtractUtils;
import cn.mw.monitor.assets.utils.ExportExcel;
import cn.mw.monitor.common.constant.ZabbixItemConstant;
import cn.mw.monitor.interceptor.DataPermUtil;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.label.api.MwLabelCommonServcie;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.user.api.MWOrgCommonService;
import cn.mw.monitor.service.user.api.MWUserCommonService;
import cn.mw.monitor.service.user.api.MWUserGroupCommonService;
import cn.mw.monitor.service.user.api.MWUserOrgCommonService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.state.DataPermission;
import cn.mw.monitor.util.MWUtils;
import cn.mw.monitor.util.TransferUtils;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.mw.monitor.service.model.util.ValConvertUtil.intValueConvert;
import static cn.mw.monitor.util.ListMapObjUtils.objectsToMaps;

/**
 * @author qzg
 * @date 2021/6/24
 */
@Service
@Slf4j
public class MwAssetsDataExportImpl implements MwAssetsDataExportService {

    private final int BATCH_INSERT_LIMIT = 500;

    @Resource
    private MwAssetsDataExportDao mwAssetsDataExportDao;
    @Autowired
    MwLabelCommonServcie mwLabelCommonServcie;
    @Autowired
    ILoginCacheInfo iLoginCacheInfo;
    @Autowired
    private MWUserGroupCommonService mwUserGroupCommonService;
    @Autowired
    private MWUserOrgCommonService mwUserOrgCommonService;
    @Autowired
    private MWOrgCommonService mwOrgCommonService;
    @Autowired
    private MWTPServerAPI mwtpServerAPI;
    @Autowired
    private MWUserCommonService mwUserCommonService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Value("${assets.host.group}")
    private Integer assetsHostGroup;


    /**
     * 每隔200条存储数据库，然后清理list ，方便内存回收
     */
    private static final int BATCH_COUNT = 200;
    // 总行数
    private static int totalRows = 0;
    // 总条数
    private static int totalCells = 0;

    @Autowired
    private MwAssetsNewFieldService assetsNewFieldService;

    @Resource
    private MwAssetsNewFieldDao assetsNewFieldDao;

    @Override
    public Reply exportForExcel(MwAssetsDataExportDto param, HttpServletRequest request, HttpServletResponse response) {
        List<MwAssetsExportTable> assetsList = selectListByExport(param);
        List<Map> mapList = new ArrayList<>();
        List<String> lable = param.getHeader();
        List<String> lableName = param.getHeaderName();
        for (MwAssetsExportTable dto : assetsList) {
            Map<String, Object> map = AttributesExtractUtils.extract(dto, lable);
            Map<String, String> customFieldValue = dto.getCustomFieldValue();
            if (customFieldValue != null) {
                map.putAll(customFieldValue);
            }
            mapList.add(map);
        }
        try {
            ExportExcel.exportExcel("资产导出", "资产导出表", lableName, lable, mapList, "yyyy-MM-dd HH:mm:ss", response);
        } catch (IOException e) {
            log.error("fail to exportForExcel param{}, case by {}", param, e);
            return Reply.fail(500, "资产导出失败");
        }
        return Reply.ok("导出成功");
    }

    /**
     * 布局数据导出Excel
     *
     * @param param
     * @param request
     * @param response
     * @return
     */
    @Override
    public Reply exportComponentLayoutForExcel(MwAssetsDataExportDto param, HttpServletRequest request, HttpServletResponse response) {
        List<Map> mapList = mwAssetsDataExportDao.exportComponentLayout(param);
        List<String> lable = Arrays.asList("assets_type_sub_id", "component_layout", "default_flag", "monitor_server_id", "template_id", "monitor_mode", "manufacturer", "specifications", "navigation_bar_id", "barName");
        List<String> lableName = Arrays.asList("assets_type_sub_id", "component_layout", "default_flag", "monitor_server_id", "template_id", "monitor_mode", "manufacturer", "specifications", "navigation_bar_id", "barName");
        try {
            ExportExcel.exportExcel("资产布局模板导出", "资产布局模板导出表", lableName, lable, mapList, "yyyy-MM-dd HH:mm:ss", response);
        } catch (IOException e) {
            log.error("fail to exportForExcel param{}, case by {}", param, e);
            return Reply.fail(500, "资产导出失败");
        }
        return Reply.ok("导出成功");
    }

    @Override
    public Reply importComponentLayoutForExcel(MultipartFile file, HttpServletResponse response) {
        //获取用户登录名
        String loginName = iLoginCacheInfo.getLoginName();
        //是否导出缺失模板的数据
        Boolean isImport = false;
        //是否覆盖导入
        Boolean isCover = false;
        //重复的NavigationBarName
        List<String> NavigationBarNames = new ArrayList<>();
        //重复NavigationBarName对应的布局数据
        List<AssetsComponentLayoutDTO> layoutDTOList = new ArrayList<>();
        try {
            Integer assetsTypeSubId = 0;
            //获取导入数据list，并插入数据
            List<AssetsComponentLayoutDTO> list = getExcelInfo(file);

            Integer userId = mwUserCommonService.getAdmin();
            //根据资产类型获取模板id、monitorServerId
            List<AssetsTemplateIdBySubTypeIdDTO> templateIdList = mwAssetsDataExportDao.getTemplateIdByAssetsSubId(assetsTypeSubId);

            //资产布局模板缺失导出
            List<AssetsComponentLayoutDTO> disList = new ArrayList<>();
            disList.addAll(list);

            List<AssetsTemplateIdBySubTypeIdDTO> disTemplateIdList = new ArrayList<>();
            disTemplateIdList.addAll(templateIdList);

            List<AssetsComponentLayoutDTO> navigations = disList.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(
                    () -> new TreeSet<>(Comparator.comparing(f -> f.getManufacturer() + "#" + f.getMonitorMode() + "#" + f.getAssetsTypeSubId()))
            ), ArrayList::new));
            ;
            List<AssetsTemplateIdBySubTypeIdDTO> disLists = TransferUtils.transferList(navigations, AssetsTemplateIdBySubTypeIdDTO.class);
            log.info("需要导入的数据disLists：" + disLists);

            for (AssetsTemplateIdBySubTypeIdDTO ast : disTemplateIdList) {
                Iterator<AssetsTemplateIdBySubTypeIdDTO> it = disLists.iterator();
                while (it.hasNext()) {
                    AssetsTemplateIdBySubTypeIdDTO dtos = it.next();
                    if (ast.getManufacturer().equals(dtos.getManufacturer()) &&
                            ast.getAssetsTypeSubId().intValue() == dtos.getAssetsTypeSubId().intValue() &&
                            ast.getMonitorMode().intValue() == dtos.getMonitorMode().intValue() &&
                            ast.getSpecification().equals(dtos.getSpecification())) {
                        it.remove();
                    }
                }
            }
            Map<Integer, String> mData = new HashMap();
            //获取资产子类型id和对应的名称
            List<Map> mList = mwAssetsDataExportDao.getSubTypeNameMap();
            log.info("获取资产子类型id和对应的名称：" + mList);
            for (Map m : mList) {
                mData.put(Integer.valueOf(m.get("id").toString()), m.get("type_name").toString());
            }
            for (AssetsTemplateIdBySubTypeIdDTO dto : disLists) {
                if (mData != null && mData.get(dto.getAssetsTypeSubId()) != null) {
                    //将子类型id转为名称
                    dto.setAssetsTypeSubName(mData.get(dto.getAssetsTypeSubId()));
                }
            }
            log.info("3333333333333：" + mList);
            List<Map> mapList = objectsToMaps(disLists);
            log.info("4444444444444：" + mapList + "；需要导出的文件个数：" + mapList.size());
            if (mapList.size() > 0) {
                //指定redis存储的key值
                String Key = "ImportLayoutInfo::Assest";
                //获取用户登录名
                String hKey = Key + "_" + loginName;
                redisTemplate.opsForValue().set(hKey, JSONObject.toJSONString(mapList), 5, TimeUnit.MINUTES);
                isImport = true;
            }
            List<Map> mapLists = mwAssetsDataExportDao.checkAll();
            Map<String, Integer> numMap = new HashMap();
            for (Map m : mapLists) {
                numMap.put(m.get("navigationBarName") + "_" + m.get("templateId"), Integer.valueOf(m.get("num").toString()));
            }
            List<Map> checkAll2Lists = mwAssetsDataExportDao.checkAll2();
            Map<String, Integer> num2Map = new HashMap();
            for (Map m : checkAll2Lists) {
                num2Map.put(m.get("navigationBarId") + "_" + m.get("templateId") + "_" + m.get("assetsTypeSubId"), Integer.valueOf(m.get("num").toString()));
            }
            List<AddNavigationBarDTO> dtoList = new ArrayList<>();
            List<AssetsComponentLayoutDTO> aclDtoList = new ArrayList<>();
            if (list != null && list.size() > 0) {
                if (templateIdList != null && templateIdList.size() > 0) {
                    for (AssetsComponentLayoutDTO param : list) {
                        for (AssetsTemplateIdBySubTypeIdDTO dto : templateIdList) {
                            //资产子类型+监控方式+品牌+规格型号 相同时，模板id相同
                            if (param.getAssetsTypeSubId().intValue() == dto.getAssetsTypeSubId().intValue()
                                    && param.getMonitorMode().intValue() == dto.getMonitorMode().intValue()
                                    && param.getManufacturer().equals(dto.getManufacturer())
                                    && param.getSpecification().equals(dto.getSpecification())) {
                                AssetsComponentLayoutDTO aclDto = new AssetsComponentLayoutDTO();
                                TransferUtils.transferBean(param, aclDto);
                                aclDto.setTemplateId(dto.getTemplateId());
                                aclDto.setUserId(userId);
                                aclDto.setCreator(loginName);
                                aclDto.setModifier(loginName);
                                aclDto.setMonitorServerId(dto.getMonitorServerId());
                                AddNavigationBarDTO addNavigationBarDTO = new AddNavigationBarDTO();
                                if (!Strings.isNullOrEmpty(aclDto.getNavigationBarName())) {
                                    //插入NavigationBar数据
                                    //根据NavigationBarName和TemplateId判断是否重复
                                    Integer checkNum = numMap.get(aclDto.getNavigationBarName() + "_" + aclDto.getTemplateId());
                                    if (checkNum != null && checkNum.intValue() == 0) {
                                        //继续新增
                                        addNavigationBarDTO.setNavigationBarName(aclDto.getNavigationBarName());
                                        addNavigationBarDTO.setTemplateId(aclDto.getTemplateId());
                                        dtoList.add(addNavigationBarDTO);
                                    }
                                    //NavigationBarName重复时， isCover为true，前端提示是否覆盖。
                                    if (checkNum != null && checkNum.intValue() > 0) {
                                        NavigationBarNames.add(aclDto.getNavigationBarName());
                                        layoutDTOList.add(aclDto);
                                        NavigationBarNames.stream().distinct();
                                        isCover = true;
                                    }
                                }
                                if (addNavigationBarDTO.getId() != null && addNavigationBarDTO.getId() != 0) {
                                    aclDto.setNavigationBarId(addNavigationBarDTO.getId());
                                }
                                Integer checkNum2 = num2Map.get(aclDto.getNavigationBarId() + "_" + aclDto.getTemplateId() + "_" + aclDto.getAssetsTypeSubId());
                                //插入数据库，判断是否存在
                                if (checkNum2 != null && checkNum2.intValue() == 0) {
                                    aclDtoList.add(aclDto);
                                }
                            }
                        }
                    }
                    if (dtoList != null && dtoList.size() > 0) {
                        mwAssetsDataExportDao.batchInsertNavigationBar(dtoList);
                    }
                    if (aclDtoList != null && aclDtoList.size() > 0) {
                        mwAssetsDataExportDao.batchInsertDataInfo(aclDtoList);
                    }
                }
            }
            //NavigationBarName相同时，是否覆盖导入
            if (isCover) {
                //将重复的值暂时存入redis中，方便调用。
                //指定redis存储的key值
                String Key = "isCoverLayout::Assest";
                //获取用户登录名
                String hKey = Key + "_" + loginName;
                redisTemplate.opsForValue().set(hKey, JSONObject.toJSONString(layoutDTOList), 5, TimeUnit.MINUTES);
            }
            Map m = new HashMap();
            m.put("isImport", isImport);
            m.put("isCover", isCover);
            List<String> names = NavigationBarNames.stream().distinct().collect(Collectors.toList());
            m.put("NavigationBarNames", names);
            return Reply.ok(m);
        } catch (Exception e) {
            log.error("fail to exportForExcel case by {}", e);
            return Reply.fail(500, "资产导出失败");
        }

    }

    /**
     * 资产布局模板缺失导出
     *
     * @param
     * @param response
     * @return
     */
    public Reply missTypeExport(HttpServletResponse response) {
        //根据key值，从redis中获取导入的file文件数据
        String Key = "ImportLayoutInfo::Assest";
        //获取用户登录名
        String loginName = iLoginCacheInfo.getLoginName();
        String hKey = Key + "_" + loginName;
        String hString = redisTemplate.opsForValue().get(hKey);
        if (Strings.isNullOrEmpty(hString)) {
            return Reply.fail("上传文件已失效，请重新上传", "");
        }
        redisTemplate.delete(hKey);
        List<Map> mapList = JSONObject.parseObject(hString, List.class);
        List<String> lable = Arrays.asList("assetsTypeSubId", "assetsTypeSubName", "monitorMode", "manufacturer");
        List<String> lableName = Arrays.asList("资产子类型", "子类型名称", "监控方式", "厂商/品牌");
        log.info("导出的文件大小111：" + mapList.size());
        try {
            if (mapList.size() > 0) {
                log.info("导出文件：" + mapList);
                ExportExcel.exportExcel("资产布局模板缺失导出", "资产布局模板缺失导出表", lableName, lable, mapList, "yyyy-MM-dd HH:mm:ss", response);
            }
        } catch (IOException e) {
            log.error("fail to missTypeExport param{}, case by {}", mapList, e);
        }
        return Reply.ok();
    }

    /**
     * 覆盖插入重复数据
     *
     * @return
     */
    @Override
    public Reply isCoverImportData() {
        //根据key值，从redis中获取导入的file文件数据
        String Key = "isCoverLayout::Assest";
        //获取用户登录名
        String loginName = iLoginCacheInfo.getLoginName();
        String hKey = Key + "_" + loginName;
        String hString = redisTemplate.opsForValue().get(hKey);
        if (Strings.isNullOrEmpty(hString)) {
            return Reply.fail("导入文件已失效，请重新导入", "");
        }
        redisTemplate.delete(hKey);
        List<AssetsComponentLayoutDTO> list = JSONArray.parseArray(hString, AssetsComponentLayoutDTO.class);
        for (AssetsComponentLayoutDTO aclDto : list) {
            AddNavigationBarDTO addNavigationBarDTO = new AddNavigationBarDTO();
            if (!Strings.isNullOrEmpty(aclDto.getNavigationBarName())) {
                //查询NavigationBarId
                addNavigationBarDTO.setNavigationBarName(aclDto.getNavigationBarName());
                addNavigationBarDTO.setTemplateId(aclDto.getTemplateId());
                Integer barId = mwAssetsDataExportDao.queryNavigationBarId(addNavigationBarDTO);
                aclDto.setNavigationBarId(barId);
            }
            Integer checkNum2 = mwAssetsDataExportDao.check2(aclDto);
            //插入数据库，判断是否存在
            if (checkNum2 != null && checkNum2.intValue() == 0) {
                //新增
                mwAssetsDataExportDao.insertDataInfo(aclDto);
            } else {
                //更新
                mwAssetsDataExportDao.updateDataInfo(aclDto);
            }
        }
        return Reply.ok();
    }

    /**
     * 查询资产列表
     * 导出使用
     *
     * @param qParam
     * @return
     */
    public List<MwAssetsExportTable> selectListByExport(MwAssetsDataExportDto qParam) {
        try {
            if (CollectionUtils.isNotEmpty(qParam.getHeader()) && qParam.getHeader().contains("itemAssetsStatus")) {
                //查询资产状态
                qParam.setQueryAssetsStatus(true);
            }
            List<MwAssetsExportTable> mwTangAssetses = new ArrayList();
            String loginName = iLoginCacheInfo.getLoginName();
            Integer userId = iLoginCacheInfo.getCacheInfo(loginName).getUserId();
            String perm = iLoginCacheInfo.getRoleInfo().getDataPerm(); //数据权限：private public
            DataPermission dataPermission = DataPermission.valueOf(perm);
            List<Integer> groupIds = mwUserGroupCommonService.getGroupIdByLoginName(loginName);//用户所在的用户组id
            if (null != groupIds && groupIds.size() > 0) {
                qParam.setGroupIds(groupIds);
            }
            if (null != qParam.getLogicalQueryLabelParamList() && qParam.getLogicalQueryLabelParamList().size() > 0) {
                List<String> assetsIds = mwLabelCommonServcie.getTypeIdsByLabel(qParam.getLogicalQueryLabelParamList());
                if (null != assetsIds && assetsIds.size() > 0) {
                    qParam.setAssetsIds(assetsIds);
                } else {
                    log.info("导出报表-ASSETS_LOG[]assets[]有形资产管理[]查询有形资产信息[]{}[]", qParam);
                    return null;
                }
            }
            switch (dataPermission) {
                case PRIVATE:
                    qParam.setUserId(userId);
                    Map priCriteria = PropertyUtils.describe(qParam);
                    mwTangAssetses = mwAssetsDataExportDao.selectPriList(priCriteria);
                    break;
                case PUBLIC:
                    String roleId = mwUserOrgCommonService.getRoleIdByLoginName(loginName);
                    List<Integer> orgIds = new ArrayList<>();
                    Boolean isAdmin = false;
                    if (roleId.equals(MWUtils.ROLE_TOP_ID)) {
                        isAdmin = true;
                    }
                    if (!isAdmin) {
                        orgIds = mwOrgCommonService.getOrgIdsByNodes(loginName);
                    }
                    if (null != orgIds && orgIds.size() > 0) {
                        qParam.setOrgIds(orgIds);
                    }
                    Map pubCriteria = PropertyUtils.describe(qParam);
                    mwTangAssetses = mwAssetsDataExportDao.selectPubList(pubCriteria);
                    break;
            }
            Map<String, String> assetsAllLabel = assetsNewFieldService.getAssetsAllLabel();//资产所有标签
            //加资产健康状态
            if (mwTangAssetses != null && mwTangAssetses.size() > 0 && qParam.isQueryAssetsStatus()) {
                Map<Integer, List<String>> groupMap = mwTangAssetses.stream().filter(s -> intValueConvert(s.getMonitorServerId()) != 0 && intValueConvert(s.getAssetsId()) != 0)
                        .collect(Collectors.groupingBy(MwAssetsExportTable::getMonitorServerId, Collectors.mapping(MwAssetsExportTable::getAssetsId, Collectors.toList())));
                Map<String, String> statusMap = new HashMap<>();
                for (Map.Entry<Integer, List<String>> value : groupMap.entrySet()) {
                    if (value.getKey() != null && value.getKey() > 0) {
                        //有改动-zabbi
                        MWZabbixAPIResult statusData = mwtpServerAPI.itemGetbySearch(value.getKey(), ZabbixItemConstant.ASSETS_STATUS, value.getValue());
                        if (!statusData.isFail()) {
                            JsonNode jsonNode = (JsonNode) statusData.getData();
                            if (jsonNode.size() > 0) {
                                for (JsonNode node : jsonNode) {
                                    Integer lastvalue = node.get("lastvalue").asInt();
                                    String hostId = node.get("hostid").asText();
                                    String status = (lastvalue.intValue() == 0) ? "异常" : "正常";
                                    statusMap.put(value.getKey() + ":" + hostId, status);
                                }
                            }
                        }
                        /*statusMap.put(value.getKey() + ":" + value.getValue(), "ABNORMAL");*/
                    }
                }
                String status = "未知";
                for (MwAssetsExportTable asset : mwTangAssetses) {
                    String s = statusMap.get(asset.getMonitorServerId() + ":" + asset.getAssetsId());
                    if (s != null && StringUtils.isNotEmpty(s)) {
                        status = s;
                    }
                    asset.setItemAssetsStatus(status);
                    //设置资产标签
                    if (assetsAllLabel != null && assetsAllLabel.size() > 0) {
                        String label = assetsAllLabel.get(asset.getId());
                        if (StringUtils.isNotBlank(label)) {
                            asset.setAssetsLabel(label);
                        }
                    }
                }
                log.info("导出报表-ASSETS_LOG[]assets[]有形资产管理[]查询有形资产信息[]{}[]", qParam);
            }


            //查询资产的自定义字段
            assetsExportCustomField(mwTangAssetses, qParam.getHeader());
            log.info("导出报表-ASSETS_LOG[]assets[]有形资产管理[]查询有形资产信息[]{}[]", qParam);
            return mwTangAssetses;
        } catch (Exception e) {
            log.error("导出报表-fail to selectList with mtaDTO={}, cause:{}", qParam, e);
            return null;
        } finally {
            log.info("导出报表-remove thread local DataPermUtil:" + DataPermUtil.getDataPerm());
            DataPermUtil.remove();
        }
    }


    /**
     * 读EXCEL文件，获取信息集合
     *
     * @return
     */
    public List<AssetsComponentLayoutDTO> getExcelInfo(MultipartFile mFile) {
        String fileName = mFile.getOriginalFilename();// 获取文件名
        try {
            if (!validateExcel(fileName)) {// 验证文件名是否合格
                return null;
            }
            boolean isExcel2003 = true;// 根据文件名判断文件是2003版本还是2007版本
            if (isExcel2007(fileName)) {
                isExcel2003 = false;
            }
            Workbook wb = null;
            if (isExcel2003) {// 当excel是2003时,创建excel2003
                wb = new HSSFWorkbook(mFile.getInputStream());
            } else {// 当excel是2007时,创建excel2007
                wb = new XSSFWorkbook(mFile.getInputStream());
            }
            //获取文件数据
            return readExcelValue(wb);// 读取Excel里面数据的信息
        } catch (Exception e) {
            log.error("读EXCEL文件失败", e);
        }
        return null;
    }


    /**
     * 读取Excel里面客户的信息
     *
     * @param wb
     * @return
     */
    private List<AssetsComponentLayoutDTO> readExcelValue(Workbook wb) {
        String loginName = iLoginCacheInfo.getLoginName();
        int num = wb.getNumberOfSheets();
        List<AssetsComponentLayoutDTO> dtoList = new ArrayList<AssetsComponentLayoutDTO>();
        Map map = new HashMap();
        for (int x = 0; x < num; x++) {
            Sheet sheet = wb.getSheetAt(x);
            // 得到Excel的行数
            totalRows = sheet.getPhysicalNumberOfRows();
            // 得到Excel的列数(前提是有行数)
            if (totalRows > 1 && sheet.getRow(0) != null) {
                totalCells = sheet.getRow(0).getPhysicalNumberOfCells();
            }
            // 循环Excel行数
            for (int r = 1; r < totalRows; r++) {
                AssetsComponentLayoutDTO dto = new AssetsComponentLayoutDTO();
                Row row = sheet.getRow(r);
                if (row == null) {
                    continue;
                }
                boolean isflag = true;
                for (int c = 0; c < totalCells; c++) {
                    Cell cell = row.getCell(c);
                    Object cellValueObj = null;
                    Object cellValue = "";
                    if (null != cell) {
                        switch (cell.getCellType()) {
                            case NUMERIC: // 数字
                                cellValueObj = new DecimalFormat("0").format(cell.getNumericCellValue());
                                cellValue = cellValueObj.toString();
                                break;
                            case STRING: // 字符串
                                cellValueObj = cell.getStringCellValue();
                                cellValue = cellValueObj.toString();
                                break;
                            case BOOLEAN: // Boolean
                                cellValue = cell.getBooleanCellValue();
                                break;
                            default:
                                break;
                        }
                    }
                    if (c == 0) {
                        //assets_type_sub_id
                        if (null == cellValue) {
                            continue;
                        } else {
                            dto.setAssetsTypeSubId(Integer.valueOf(cellValue.toString()));
                        }

                    } else if (c == 1) {
                        //component_layout
                        if (null == cellValue) {
                            isflag = false;
                            continue;
                        } else {
                            dto.setComponentLayout(cellValue.toString());
                        }

                    } else if (c == 2) {
                        //default_flag
                        if (null == cellValue) {
                            isflag = false;
                            continue;
                        }
                        dto.setDefaultFlag(Boolean.valueOf(cellValue.toString()));
                    } else if (c == 3) {
                        //monitor_server_id
                        if (null == cellValue) {
                            isflag = false;
                            continue;
                        } else {
                            dto.setMonitorServerId(Integer.valueOf(cellValue.toString()));
                        }
                    } else if (c == 4) {
                        //template_id
                        if (null == cellValue) {
                            isflag = false;
                            continue;
                        } else {
                            dto.setTemplateId(cellValue.toString());
                        }
                    } else if (c == 5) {
                        //monitor_mode
                        if (null == cellValue) {
                            isflag = false;
                            continue;
                        } else {
                            dto.setMonitorMode(Integer.valueOf(cellValue.toString()));
                        }
                    } else if (c == 6) {
                        //manufacture
                        if (null == cellValue) {
                            isflag = false;
                            continue;
                        } else {
                            dto.setManufacturer(cellValue.toString());
                        }
                    } else if (c == 7) {
                        //specifications
                        if (null == cellValue) {
                            isflag = false;
                            continue;
                        } else {
                            dto.setSpecification(cellValue.toString());
                        }
                    } else if (c == 8) {
                        //navigation_bar_id
                        if (null == cellValue) {
                            isflag = false;
                            continue;
                        } else {
                            dto.setNavigationBarId(Integer.valueOf(cellValue.toString()));
                        }
                    } else if (c == 9) {
                        //barName
                        dto.setNavigationBarName(cellValue.toString());
                    }
                }
                if (isflag) {//导入数据有错误，就不添加
                    dto.setCreator(loginName);
                    dto.setModifier(loginName);
                    dtoList.add(dto);
                }
            }
        }
        return dtoList;
    }


    /**
     * 验证EXCEL文件
     *
     * @param filePath
     * @return
     */
    public static boolean validateExcel(String filePath) {
        if (filePath == null || !(isExcel2003(filePath) || isExcel2007(filePath))) {
            return false;
        }
        return true;
    }

    // @描述：是否是2003的excel，返回true是2003
    public static boolean isExcel2003(String filePath) {
        return filePath.matches("^.+\\.(?i)(xls)$");
    }

    // @描述：是否是2007的excel，返回true是2007
    public static boolean isExcel2007(String filePath) {
        return filePath.matches("^.+\\.(?i)(xlsx)$");
    }

    /**
     * 资产导出自定义字段
     *
     * @param mwTangAssetses
     * @param lable
     */
    private void assetsExportCustomField(List<MwAssetsExportTable> mwTangAssetses, List<String> lable) {
        List<MwAssetsCustomFieldDto> mwAssetsCustomFieldDtos = assetsNewFieldDao.selectAssetsCustomField();
        if (CollectionUtils.isEmpty(mwAssetsCustomFieldDtos)) return;
        Iterator<MwAssetsCustomFieldDto> iterator = mwAssetsCustomFieldDtos.iterator();
        while (iterator.hasNext()) {
            MwAssetsCustomFieldDto next = iterator.next();
            if (!lable.contains(next.getProp())) {
                iterator.remove();
            }
        }
        log.info("获取自定义字段::"+mwAssetsCustomFieldDtos);
        //获取自定义字段值
        getCustomFieldValue(mwAssetsCustomFieldDtos, mwTangAssetses);
    }

    /**
     * 获取自定义字段的值
     *
     * @param mwAssetsCustomFieldDtos
     * @param mwTangAssetses
     */
    private void getCustomFieldValue(List<MwAssetsCustomFieldDto> mwAssetsCustomFieldDtos, List<MwAssetsExportTable> mwTangAssetses) {
        if (CollectionUtils.isEmpty(mwAssetsCustomFieldDtos) || CollectionUtils.isEmpty(mwTangAssetses)) return;
        Map<Integer, List<String>> groupMap = mwTangAssetses.stream()
                .collect(Collectors.groupingBy(MwAssetsExportTable::getMonitorServerId, Collectors.mapping(MwAssetsExportTable::getAssetsId, Collectors.toList())));
        List<String> itemNames = new ArrayList<>();
        Map<String, String> customFieldMap = new HashMap<>();
        List<MwAssetsCustomFieldDto> labelAssetsCustomFieldDtos = new ArrayList<>();
        //取出监控项名称与字段名称
        mwAssetsCustomFieldDtos.forEach(value -> {
            if (value.getType() == 1) {
                itemNames.add(value.getProp());
                customFieldMap.put(value.getProp(), value.getLabel());
            } else {
                labelAssetsCustomFieldDtos.add(value);
            }
        });
        Map<String, List<String>> map = new HashMap<>();
        //根据分组查询zabbix接口获取数据
        for (Map.Entry<Integer, List<String>> value : groupMap.entrySet()) {
            MWZabbixAPIResult result = mwtpServerAPI.itemGetbySearch(value.getKey(), itemNames, value.getValue());
            if (result != null && !result.isFail() && result.getData() != null) {
                JsonNode jsonNode = (JsonNode) result.getData();
                if (jsonNode != null && jsonNode.size() > 0) {
                    for (JsonNode node : jsonNode) {
                        String lastvalue = node.get("lastvalue").asText();//最新值
                        String hostId = node.get("hostid").asText();//主机ID
                        String units = "";//单位
                        if (node.get("units") != null) {
                            units = node.get("units").asText();
                        }
                        String name = node.get("name").asText();//监控名称
                        if (lastvalue == null || StringUtils.isBlank(hostId)) continue;
                        if (map != null && map.get(hostId) != null) {
                            List<String> values = map.get(hostId);
                            values.add(lastvalue + units + "," + name);
                            map.put(hostId, values);
                        } else {
                            List<String> values = new ArrayList<>();
                            values.add(lastvalue + units + "," + name);
                            map.put(hostId, values);
                        }
                    }
                }
            }
        }
        //设置资产自定义字段值1
        if (!map.isEmpty()) {
            for (MwAssetsExportTable mwTangAssets : mwTangAssetses) {
                Map<String, String> assetsMap = new HashMap<>();
                String assetsId = mwTangAssets.getAssetsId();
                List<String> values = map.get(assetsId);
                if (CollectionUtils.isEmpty(values)) continue;
                values.forEach(value -> {
                    for (String key : customFieldMap.keySet()) {
                        if ((value.split(",")[1]).contains(key)) {
                            assetsMap.put(key, value.split(",")[0]);
                            continue;
                        }
                    }
                });
                mwTangAssets.setCustomFieldValue(assetsMap);
            }
        }
        //处理标签自定义字段
        handleLabelCustomField(labelAssetsCustomFieldDtos, mwTangAssetses);
    }

    /**
     * 处理标签自定义字段
     *
     * @param labelAssetsCustomFieldDtos
     */
    private void handleLabelCustomField(List<MwAssetsCustomFieldDto> labelAssetsCustomFieldDtos, List<MwAssetsExportTable> mwTangAssetses) {
        if (CollectionUtils.isEmpty(labelAssetsCustomFieldDtos) || CollectionUtils.isEmpty(mwTangAssetses)) return;
        //获取标签名称集合
        List<String> labelNames = new ArrayList<>();
        labelAssetsCustomFieldDtos.forEach(value -> {
            labelNames.add(value.getProp());
        });
        if (CollectionUtils.isEmpty(labelNames)) return;
        //根据标签名称查询资产对应标签信息
        List<Map<String, Object>> labelMaps = assetsNewFieldDao.selectAssetsLabelByLabelName(labelNames);
        if (CollectionUtils.isEmpty(labelMaps)) return;
        for (Map<String, Object> map : labelMaps) {
            Object typeId = map.get("typeId");//资产ID
            Object labelName = map.get("labelName");//标签名称
            Object labelValue = map.get("labelValue");//标签值
            for (MwAssetsExportTable mwTangAssets : mwTangAssetses) {
                if (typeId == null || labelName == null || labelValue == null || !mwTangAssets.getId().equals(typeId.toString()))
                    continue;
                Map<String, String> customFieldValue = mwTangAssets.getCustomFieldValue();
                if (customFieldValue == null) {
                    customFieldValue = new HashMap<>();
                }
                customFieldValue.put(labelName.toString(), labelValue.toString());
                mwTangAssets.setCustomFieldValue(customFieldValue);
            }
        }
    }

    /**
     * 导出资产所有指标
     * @param response
     */
    @Override
    public void exportAssetsIndex(HttpServletResponse response) {
        //获取资产信息
        QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
        assetsParam.setPageNumber(1);
        assetsParam.setPageSize(Integer.MAX_VALUE);
        assetsParam.setIsQueryAssetsState(false);
        assetsParam.setUserId(mwUserCommonService.getAdmin());
        List<MwTangibleassetsTable> assetsTable = mwAssetsManager.getAssetsTable(assetsParam);
        //分组
        Map<Integer, List<String>> groupMap = assetsTable.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0)
                .collect(Collectors.groupingBy(MwTangibleassetsTable::getMonitorServerId, Collectors.mapping(MwTangibleassetsTable::getAssetsId, Collectors.toList())));
        Map<String,MwTangibleassetsTable> assetsMap = new HashMap<>();
        assetsTable.forEach(item->{
            assetsMap.put(item.getMonitorServerId()+item.getAssetsId(),item);
        });
        List<AssetsExportIndexDto> exportIndexDtos = new ArrayList<>();
        for (Map.Entry<Integer, List<String>> entry : groupMap.entrySet()) {
            Integer serverId = entry.getKey();
            List<String> hostIds = entry.getValue();
            List<List<String>> partition = Lists.partition(hostIds, assetsHostGroup);
            for (List<String> ids : partition) {
                MWZabbixAPIResult result = mwtpServerAPI.itemGetbySearch(serverId, null, ids);
                if(result == null || result.isFail()){continue;}
                List<ItemApplication> itemApplications = JSONArray.parseArray(String.valueOf(result.getData()), ItemApplication.class);
                for (ItemApplication itemApplication : itemApplications) {
                    AssetsExportIndexDto indexDto = new AssetsExportIndexDto();
                    indexDto.extractFrom(itemApplication,assetsMap.get(serverId+itemApplication.getHostid()));
                    exportIndexDtos.add(indexDto);
                }
            }
        }
        //数据导出
        exportIndex(exportIndexDtos,response);
    }


    private void exportIndex(List<AssetsExportIndexDto> exportIndexDtos,HttpServletResponse response){
        ExcelWriter excelWriter = null;
        try {
            excelWriter = exportSetNews(response, AssetsExportIndexDto.class);
            HashSet<String> includeColumnFiledNames = new HashSet<>();
            includeColumnFiledNames.add("assetsId");
            includeColumnFiledNames.add("serverId");
            includeColumnFiledNames.add("hostId");
            includeColumnFiledNames.add("assetsName");
            includeColumnFiledNames.add("ipAddress");
            includeColumnFiledNames.add("itemName");
            WriteSheet sheet = EasyExcel.writerSheet(0, "sheet")
                    .includeColumnFiledNames(includeColumnFiledNames)
                    .build();
            excelWriter.write(exportIndexDtos, sheet);
        }catch (Throwable e){
            log.error("MwAssetsDataExportImpl{} exportIndex() error",e);
        }finally {
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    private ExcelWriter exportSetNews(HttpServletResponse response,Class dtoclass) throws IOException {
        String fileName = System.currentTimeMillis()+""; //导出文件名
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
        // 头的策略
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontHeightInPoints((short) 11);
        headWriteCellStyle.setWriteFont(headWriteFont);
        // 内容的策略
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        WriteFont contentWriteFont = new WriteFont();
        // 字体大小
        contentWriteFont.setFontHeightInPoints((short) 12);
        contentWriteCellStyle.setWriteFont(contentWriteFont);
        // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
        HorizontalCellStyleStrategy horizontalCellStyleStrategy=new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
        //创建easyExcel写出对象
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), dtoclass).registerWriteHandler(horizontalCellStyleStrategy).build();
        return excelWriter;
    }
}
