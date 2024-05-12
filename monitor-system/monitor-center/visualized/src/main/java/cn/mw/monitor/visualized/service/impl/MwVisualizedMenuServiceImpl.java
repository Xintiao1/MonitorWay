package cn.mw.monitor.visualized.service.impl;

import cn.mw.monitor.api.param.org.QueryOrgForDropDown;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.server.api.dto.ItemApplication;
import cn.mw.monitor.service.user.dto.MWOrgDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.user.service.MWOrgService;
import cn.mw.monitor.util.UnitsUtil;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dao.MwVisualizedMenuDao;
import cn.mw.monitor.visualized.dto.*;
import cn.mw.monitor.visualized.service.MwVisualizedManageService;
import cn.mw.monitor.visualized.service.MwVisualizedMenuService;
import cn.mw.monitor.visualized.util.MwVisualizedUtil;
import cn.mw.zbx.MWTPServerAPI;
import cn.mw.zbx.MWZabbixAPIResult;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import cn.mwpaas.common.utils.UUIDUtils;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName MwVisualizedChartServiceImpl
 * @Author gengjb
 * @Date 2022/4/14 10:28
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedMenuServiceImpl implements MwVisualizedMenuService {

    @Resource
    private MwVisualizedMenuDao visualizedChartDao;

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @Value("${visualized.upload}")
    private String filePath;

    @Value("${visualized.backgroud}")
    private String backGroudFilePath;

    @Autowired
    MWOrgService mwOrgService;

    @Resource
    MwVisualizedManageDao visualizedManageDao;

    @Autowired
    private MWTPServerAPI mwtpServerAPI;

    @Autowired
    private MwVisualizedManageService mwVisualizedManageService;

    @Autowired
    private MwAssetsManager assetsManager;

    /**
     * 查询分区与类型数据
     * @return
     */
    @Override
    public Reply selectVisualizedChart() {
        try {
            //查询数据
            List<MwVisualizedChartDto> mwVisualizedChartDtos = visualizedChartDao.selectVisualizedChart();
            //数据处理，处理成父子结构数据
            if(CollectionUtils.isEmpty(mwVisualizedChartDtos))return Reply.ok(mwVisualizedChartDtos);
            List<MwVisualizedChartDto> realData = new ArrayList<>();
            for (MwVisualizedChartDto mwVisualizedChartDto : mwVisualizedChartDtos) {
                Integer parentId = mwVisualizedChartDto.getParentId();
                if(parentId == null){
                    realData.add(mwVisualizedChartDto);
                }
                String iconUrl = mwVisualizedChartDto.getIconUrl();//图标图片
                String dragUrl = mwVisualizedChartDto.getDragUrl();//拖拽图片
                if(StringUtils.isNotBlank(iconUrl)){
                    mwVisualizedChartDto.setIconUrl(filePath+iconUrl);
                }
                if(StringUtils.isNotBlank(dragUrl)){
                    mwVisualizedChartDto.setDragUrl(filePath+dragUrl);
                }
            }
            //处理子数据
            for (MwVisualizedChartDto realDatum : realData) {
                Integer id = realDatum.getId();
                String partition = realDatum.getPartition();
                String partitionName = realDatum.getPartitionName();
                List<MwVisualizedChartDto> visualizedChartDtos = realDatum.getChildren();
                if(visualizedChartDtos == null){
                    visualizedChartDtos = new ArrayList<>();
                }
                //根据ID判断，如果ID与子父ID相等，说明是该分区下子类型
                for (MwVisualizedChartDto mwVisualizedChartDto : mwVisualizedChartDtos) {
                    Integer parentId = mwVisualizedChartDto.getParentId();
                    if(id != null && parentId != null && id == parentId){
                        mwVisualizedChartDto.setParentPartition(partition);
                        mwVisualizedChartDto.setParentPartitionName(partitionName);
                        visualizedChartDtos.add(mwVisualizedChartDto);
                    }
                }
                realDatum.setChildren(visualizedChartDtos);
            }
            return Reply.ok(realData);
        }catch (Throwable e){
            log.error("查询可视化分区类型失败,失败信息:",e);
            return Reply.fail("查询可视化分区类型失败"+e.getMessage());
        }
    }


    /**
     * 查询可视化维度
     * @return
     */
    @Override
    public Reply selectVisualizedDimension() {
        try {
            List<MwVisualizedDimensionDto> realData = new ArrayList<>();
            Map<String,List<MwVisualizedDimensionDto>> treeMap = new HashMap<>();
            //查询品牌
            List<MwVisualizedDimensionDto> vendorList = visualizedChartDao.selectAssetsVendorList();
            if(CollectionUtils.isNotEmpty(vendorList)){
                for (MwVisualizedDimensionDto dimensionDto : vendorList) {
                    dimensionDto.setUrl_type(2);
                    dimensionDto.setChildren(null);
                }
            }
            MwVisualizedDimensionDto dimensionDto = new MwVisualizedDimensionDto();
            dimensionDto.setChildren(vendorList);
            dimensionDto.setTypeName("品牌");
            dimensionDto.setUrl_type(1);
            realData.add(dimensionDto);
            //查询资产类型
            List<MwVisualizedDimensionDto> typeList = visualizedChartDao.selectVisualizedAssetsTypeList();
            if(CollectionUtils.isNotEmpty(typeList)){
                for (MwVisualizedDimensionDto dimensionDto2 : typeList) {
                    dimensionDto2.setUrl_type(2);
                    dimensionDto2.setChildren(null);
                }
            }
            MwVisualizedDimensionDto dimensionDto2 = new MwVisualizedDimensionDto();
            dimensionDto2.setChildren(typeList);
            dimensionDto2.setTypeName("资产类型");
            dimensionDto2.setUrl_type(1);
            realData.add(dimensionDto2);
            //查询资产标签
            List<MwVisualizedDimensionDto> labelList = visualizedChartDao.selectVisualizedAssetsLabelList();
            if(CollectionUtils.isNotEmpty(labelList)){
                for (MwVisualizedDimensionDto dimensionDto4 : labelList) {
                    dimensionDto4.setUrl_type(2);
                    dimensionDto4.setChildren(null);
                }
            }
            MwVisualizedDimensionDto dimensionDto3 = new MwVisualizedDimensionDto();
            dimensionDto3.setChildren(labelList);
            dimensionDto3.setTypeName("标签");
            dimensionDto3.setUrl_type(1);
            realData.add(dimensionDto3);
            //查询资产机构
            List<MwVisualizedDimensionDto> orgList = getOrgAssets();
            if(CollectionUtils.isNotEmpty(orgList)){
                for (MwVisualizedDimensionDto dimensionDto4 : orgList) {
                    dimensionDto4.setUrl_type(2);
                    dimensionDto4.setChildren(null);
                }
            }
            MwVisualizedDimensionDto dimensionDto4 = new MwVisualizedDimensionDto();
            dimensionDto4.setChildren(orgList);
            dimensionDto4.setTypeName("机构");
            dimensionDto4.setUrl_type(1);
            realData.add(dimensionDto4);
            //查询所有资产
            List<MwVisualizedDimensionDto> allAssets = getAllAssets();
            if(CollectionUtils.isNotEmpty(allAssets)){
                for (MwVisualizedDimensionDto dimensionDto5 : allAssets) {
                    dimensionDto5.setUrl_type(2);
                    dimensionDto5.setChildren(null);
                }
            }
            MwVisualizedDimensionDto dimensionDto5 = new MwVisualizedDimensionDto();
            if(CollectionUtils.isNotEmpty(allAssets)){
                for (MwVisualizedDimensionDto allAsset : allAssets) {
                    allAsset.setUrl_type(2);
                }
            }
            dimensionDto5.setChildren(allAssets);
            dimensionDto5.setTypeName("资产");
            treeMap.put("资产",allAssets);
            dimensionDto5.setUrl_type(1);
            realData.add(dimensionDto5);

            MwVisualizedDimensionDto dimensionDto6 = new MwVisualizedDimensionDto();
            dimensionDto6.setChildren(realData);
            dimensionDto6.setTypeName("维度");
            visualizedDimensionHandler(dimensionDto6);
            return Reply.ok(dimensionDto6);
        }catch (Exception e){
            log.error("查询可视化维度信息失败,失败信息:",e);
            return Reply.fail("查询可视化维度信息失败"+e.getMessage());
        }
    }


    private void visualizedDimensionHandler(MwVisualizedDimensionDto dimensionDto){
        dimensionDto.setUuid(UUID.randomUUID().toString());
        List<MwVisualizedDimensionDto> children = dimensionDto.getChildren();
        if(CollectionUtils.isNotEmpty(children)){
            for (MwVisualizedDimensionDto child : children) {
                visualizedDimensionHandler(child);
            }
        }
    }

    private List<MwVisualizedDimensionDto> getAllAssets(){
        QueryTangAssetsParam param = new QueryTangAssetsParam();
        param.setPageNumber(1);
        param.setPageSize(Integer.MAX_VALUE);
        List<MwTangibleassetsTable> mwTangibleassetsTables = assetsManager.getAssetsTable(param);
        List<MwVisualizedDimensionDto> list = new ArrayList<>();
        if(CollectionUtils.isEmpty(mwTangibleassetsTables))return list;
        for (MwTangibleassetsTable mwTangibleassetsTable : mwTangibleassetsTables) {
            String assetsName = mwTangibleassetsTable.getAssetsName();
            String assetsId = mwTangibleassetsTable.getAssetsId();
            String id = mwTangibleassetsTable.getId();
            Integer monitorServerId = mwTangibleassetsTable.getMonitorServerId();
            MwVisualizedDimensionDto dimensionDto = new MwVisualizedDimensionDto();
            List<MwVisualizedAssetsDto> assetsDtos = new ArrayList<>();
            MwVisualizedAssetsDto assetsDto = new MwVisualizedAssetsDto();
            dimensionDto.setTypeName(assetsName);
            assetsDto.setAssetsId(assetsId);
            assetsDto.setId(id);
            assetsDto.setMonitorServerId(monitorServerId);
            assetsDto.setIpAddress(mwTangibleassetsTable.getInBandIp());
            assetsDto.setAssetsName(assetsName);
            assetsDtos.add(assetsDto);
            dimensionDto.setAssetsList(assetsDtos);
            list.add(dimensionDto);
        }
        return list;
    }


    /**
     * 查询机构分类的资产数据
     * @return
     */
    private List<MwVisualizedDimensionDto> getOrgAssets(){
        List<MwVisualizedDimensionDto> list = new ArrayList<>();
        //查询机构
        List<MwVisualizedAssetsDto> orgIds = new ArrayList<>();
        HashSet<String> assetsIdSet = new HashSet<>();
        HashSet<Integer> userOrgSet = new HashSet<>();
        Reply userOrgReply = mwOrgService.selectDorpdownList(new QueryOrgForDropDown());
        if (userOrgReply.getRes() == PaasConstant.RES_SUCCESS ){
            List<MWOrgDTO> orgList = (List<MWOrgDTO>) userOrgReply.getData();
            getUserOrgSet(orgList,userOrgSet);
        }
        Map<String,Object> queryParam = new HashMap<>();
        List<MWOrgDTO> orgList = mwOrgService.getAllOrgList();
        if (CollectionUtils.isNotEmpty(orgList)){
            for (MWOrgDTO org : orgList) {
                MwVisualizedDimensionDto dimensionDto = new MwVisualizedDimensionDto();
                dimensionDto.setTypeName(org.getOrgName());
                dimensionDto.setTypeId(org.getOrgId());
                List<MwVisualizedAssetsDto> visualizedAssetsDtos = new ArrayList<>();
                if (userOrgSet.contains(org.getOrgId())){
                    queryParam.put("orgId",org.getOrgId());
                    visualizedAssetsDtos    = visualizedChartDao.selectVisualizedAssetsOrgList(queryParam);
                    if (CollectionUtils.isNotEmpty(visualizedAssetsDtos)) {
                        Iterator iterator = visualizedAssetsDtos.iterator();
                        while (iterator.hasNext()) {
                            MwVisualizedAssetsDto assetsDTO = (MwVisualizedAssetsDto) iterator.next();
                            if (assetsDTO != null && StringUtils.isNotEmpty(assetsDTO.getAssetsId())) {
                                orgIds.add(assetsDTO);
                                assetsIdSet.add(assetsDTO.getId());
                            } else {
                                iterator.remove();
                            }
                        }
                    }
                }
                if (org.getChilds() != null && org.getChilds().size() > 0) {
                    visualizedAssetsDtos.addAll(getChildOrgAssetsList(queryParam, dimensionDto, org.getChilds(),
                            orgIds, assetsIdSet, userOrgSet));
                }
                if (CollectionUtils.isNotEmpty(visualizedAssetsDtos)) {
                    visualizedAssetsDtos = visualizedAssetsDtos.stream().distinct().collect(Collectors.toList());
                    dimensionDto.setAssetsList(visualizedAssetsDtos);
                    list.add(dimensionDto);
                }else{
                    dimensionDto.setAssetsList(visualizedAssetsDtos);
                    list.add(dimensionDto);
                }
            }
        }
        return list;
    }


    private void getUserOrgSet(List<MWOrgDTO> orgList, HashSet<Integer> userOrgSet) {
        if (CollectionUtils.isNotEmpty(orgList)){
            for (MWOrgDTO org : orgList){
                userOrgSet.add(org.getOrgId());
                if (CollectionUtils.isNotEmpty(org.getChilds())){
                    getUserOrgSet(org.getChilds(),userOrgSet);
                }
            }
        }
    }

    /**
     * 查询可视化指标数据
     * @return
     */
    @Override
    public Reply selectVisualizedIndex(MwVisualizedIndexDto dtos) {
        try {
            List<MwVisualizedAssetsDto> params = dtos.getParams();
            //数据分组
            Map<Integer,List<String>> hostMap = params.stream().filter(item->item.getMonitorServerId() != null &&  item.getMonitorServerId() != 0)
                    .collect(Collectors.groupingBy(MwVisualizedAssetsDto::getMonitorServerId, Collectors.mapping(MwVisualizedAssetsDto::getAssetsId, Collectors.toList())));
            List<MwVisualizedIndexDto> realData = new ArrayList<>();
            //查询最新数据
            List<ItemApplication> itemApplications = getNewestData(hostMap,dtos.getItemNames());
            //查询所有指标数据
            List<MwVisualizedIndexDto> mwVisualizedIndexDtos = visualizedChartDao.selectVisualizedIndex();
            //设置指标数据
            if(CollectionUtils.isNotEmpty(itemApplications)){
                for (ItemApplication application : itemApplications){
                    MwVisualizedIndexDto mwVisualizedIndexDto = new MwVisualizedIndexDto();
                    String itemName = application.getName();
                    if(StringUtils.isNotBlank(itemName) && itemName.contains("]")){
                        //去除中括号
                        itemName = itemName.replace(itemName.substring(itemName.indexOf("["),itemName.indexOf("]")+1),"");
                    }
                    for (MwVisualizedIndexDto visualizedIndexDto : mwVisualizedIndexDtos) {
                        String indexMonitorItem = visualizedIndexDto.getIndexMonitorItem();//监控项名称
                        if(itemName.equals(indexMonitorItem)){
                            if(application.getName().contains("[") && application.getName().contains("]")){
                                String interfaceName = application.getName().substring(itemName.indexOf("[")+1,application.getName().indexOf("]"));
                                mwVisualizedIndexDto.setInterfaceName(interfaceName);
                            }
                            mwVisualizedIndexDto.setIndexMonitorItem(indexMonitorItem);
                            mwVisualizedIndexDto.setIndexName(visualizedIndexDto.getIndexName());
                            continue;
                        }
                    }
                    //设置资产信息
                    for (MwVisualizedAssetsDto param : params) {
                        String assetsId = param.getAssetsId();
                        String assetsName = param.getAssetsName();
                        Integer monitorServerId = param.getMonitorServerId();
                        if(StringUtils.isBlank(assetsId) || StringUtils.isBlank(application.getHostid())){continue;}
                        if(assetsId.equals(application.getHostid())){
                            mwVisualizedIndexDto.setAssetsId(assetsId);
                            mwVisualizedIndexDto.setAssetsName(assetsName);
                            mwVisualizedIndexDto.setMonitorServerId(monitorServerId);
                            mwVisualizedIndexDto.setIpAddress(param.getIpAddress());
                            continue;
                        }
                    }
                    if(StringUtils.isBlank(application.getLastvalue())){continue;}
                    Map<String, String> valueMap = new HashMap<>();
                    if(MwVisualizedUtil.checkStrIsNumber(application.getLastvalue())){
                        valueMap = UnitsUtil.getConvertedValue(new BigDecimal(application.getLastvalue()),application.getUnits());
                    }
                    //设置当前值
                    if(valueMap == null || valueMap.isEmpty()){
                        mwVisualizedIndexDto.setCurrValue(application.getLastvalue());
                        mwVisualizedIndexDto.setNumberType(application.getUnits());
                    }else{
                        mwVisualizedIndexDto.setCurrValue(valueMap.get("value"));
                        mwVisualizedIndexDto.setNumberType(valueMap.get("units"));
                    }
                    mwVisualizedIndexDto.setOriginUnits(application.getUnits());
                    mwVisualizedIndexDto.setItemId(application.getItemid());
                    mwVisualizedIndexDto.setValueType(Integer.parseInt(application.getValue_type()));
                    realData.add(mwVisualizedIndexDto);
                }
            }
            if(CollectionUtils.isNotEmpty(realData)){
                for (int i = 0; i < realData.size(); i++) {
                    MwVisualizedIndexDto indexDto = realData.get(i);
                    indexDto.setOnlyId(i+1);
                }
            }
            //处理分页信息
            Integer pageNumber = dtos.getPageNumber();
            Integer pageSize = dtos.getPageSize();
            int fromIndex = pageSize * (pageNumber -1);
            int toIndex = pageSize * pageNumber;
            if(toIndex > realData.size()){
                toIndex = realData.size();
            }
            if(fromIndex > realData.size()){
                fromIndex = realData.size();
            }
            List<MwVisualizedIndexDto> pageList = realData.subList(fromIndex, toIndex);
            PageInfo pageInfo = new PageInfo<>(pageList);
            pageInfo.setTotal(realData.size());
            pageInfo.setList(pageList);
            return Reply.ok(pageInfo);
        }catch (Throwable e){
            log.error("查询可视化指标信息失败,失败信息:",e);
            return Reply.fail("查询可视化指标信息失败"+e.getMessage());
        }
    }

    /**
     * 指标获取数据源最新数据
     */
    private List<ItemApplication> getNewestData( Map<Integer,List<String>> hostMap,List<String> itemNmaes){
        List<ItemApplication> itemApplications = new ArrayList<>();
        for (Integer serverId : hostMap.keySet()) {
            List<String> hostIds = hostMap.get(serverId);
            //查询zabbix监控项信息
            MWZabbixAPIResult result = mwtpServerAPI.itemGetbyType(serverId, itemNmaes, hostIds, false);
            if(result == null || result.isFail()){continue;}
            itemApplications.addAll(JSONArray.parseArray(String.valueOf(result.getData()), ItemApplication.class));
        }
        return itemApplications;
    }


    /**
     * 视化增加图形图片上传
     * @param multipartFile 图片信息
     * @return
     */
    @Override
    public Reply addVisualizedImageUpload(MultipartFile multipartFile,Integer id) {
        try {
            if (multipartFile.isEmpty()) {
                Reply.fail("文件为空");
            }
            //获取文件名
            String fileName = multipartFile.getOriginalFilename();
            //获取文件的后缀名
            String suffixName = fileName.substring(fileName.lastIndexOf("."));
            //设置放到数据库字段的值
            String fileNameInTable = UUIDUtils.getUUID() + suffixName;
            File file = new File(new File(backGroudFilePath).getAbsolutePath()
                    + File.separator + File.separator + fileNameInTable);
            //检测是否存在目录
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            multipartFile.transferTo(file);
            Runtime runtime = Runtime.getRuntime();
//            String command = "chmod 644 " + file.getAbsolutePath();
//            Process process = runtime.exec(command);
//            process.waitFor();
            //上传成功后关联视图
            MwVisualizedViewDto visualizedViewDto = new MwVisualizedViewDto();
            visualizedViewDto.setId(id);
            visualizedViewDto.setBackGroundImage(fileNameInTable);
            mwVisualizedManageService.updateVisualizedView(visualizedViewDto);
            return Reply.ok(fileNameInTable);
        }catch (Exception e){
            log.error("上传可视化图片失败,失败信息:",e);
            return Reply.fail("上传可视化图片失败"+e.getMessage());
        }
    }

    /**
     * 添加可视化分区类型数据
     * @param visualizedChartDto
     * @return
     */
    @Override
    public Reply addVisualizedChart(MwVisualizedChartDto visualizedChartDto) {
        try {
            visualizedChartDto.setCreator(iLoginCacheInfo.getLoginName());
            visualizedChartDto.setCreateDate(new Date());
            //进行数据添加
            int count = visualizedChartDao.addVisualizedChart(visualizedChartDto);
            if(count > 0){
                return Reply.ok("添加成功");
            }
            return Reply.fail("添加可视化分区类型失败");
        }catch (Exception e){
            log.error("添加可视化分区类型失败,失败信息:",e);
            return Reply.fail("添加可视化分区类型失败"+e.getMessage());
        }
    }


//    /**
//     * 修改可视化分区或者类型数据
//     * @param visualizedChartDto
//     * @return
//     */
//    @Override
//    public Reply updateVisualizedChart(MwVisualizedChartDto visualizedChartDto) {
//        try {
//            visualizedChartDto.setModifier(iLoginCacheInfo.getLoginName());
//            visualizedChartDto.setModificationDate(new Date());
//            //进行数据添加
//            int count = visualizedChartDao.addVisualizedChart(visualizedChartDto);
//            if(count > 0){
//                return Reply.ok("添加成功");
//            }
//            return Reply.fail("添加可视化分区类型失败");
//        }catch (Exception e){
//            log.error("添加可视化分区类型失败,失败信息:",e);
//            return Reply.fail("添加可视化分区类型失败"+e.getMessage());
//        }
//    }




    /**
     * 获取子级机构的资产数据
     *
     * @param queryParam    查询参数
     * @param assetsTreeDTO 上级资产数据
     * @param childs        子机构列表数据
     * @param orgIds        已添加的资产数据
     * @param assetsIdSet   已添加的资产ID
     * @param userOrgSet    用户的机构ID集合
     */
    private List<MwVisualizedAssetsDto> getChildOrgAssetsList(Map<String, Object> queryParam, MwVisualizedDimensionDto assetsTreeDTO,
                                                  List<MWOrgDTO> childs, List<MwVisualizedAssetsDto> orgIds,
                                                  HashSet<String> assetsIdSet, HashSet<Integer> userOrgSet) {
        //当前机构及子机构的所有资产数据
        List<MwVisualizedAssetsDto> allAssetsList = new ArrayList<>();
        for (MWOrgDTO child : childs) {
            //下级的资产数据
            List<MwVisualizedAssetsDto> lowerAssetsList = new ArrayList<>();
            //下级资产树状数据
            MwVisualizedDimensionDto assetsTreeChild = new MwVisualizedDimensionDto();
            assetsTreeChild.setTypeName(child.getOrgName());
            assetsTreeChild.setTypeId(child.getOrgId());
            List<MwVisualizedAssetsDto> childAssetsDTOList = new ArrayList<>();
            if (userOrgSet.contains(child.getOrgId())) {
                queryParam.put("orgId", child.getOrgId());
                //获取当前机构的资产数据
                childAssetsDTOList = visualizedChartDao.selectVisualizedAssetsOrgList(queryParam);
                if (CollectionUtils.isNotEmpty(childAssetsDTOList)) {
                    Iterator iterator = childAssetsDTOList.iterator();
                    while (iterator.hasNext()) {
                        MwVisualizedAssetsDto assetsDTO = (MwVisualizedAssetsDto) iterator.next();
                        if (assetsDTO != null && StringUtils.isNotEmpty(assetsDTO.getAssetsId())) {
                            orgIds.add(assetsDTO);
                            assetsIdSet.add(assetsDTO.getId());
                        } else {
                            iterator.remove();
                        }
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(child.getChilds())) {
                lowerAssetsList.addAll(getChildOrgAssetsList(queryParam, assetsTreeChild, child.getChilds(), orgIds, assetsIdSet, userOrgSet));
            }
            if (CollectionUtils.isNotEmpty(childAssetsDTOList) || CollectionUtils.isNotEmpty(lowerAssetsList)) {
                childAssetsDTOList.addAll(lowerAssetsList);
                childAssetsDTOList.stream().distinct().collect(Collectors.toList());
                assetsTreeChild.setAssetsList(childAssetsDTOList);
                assetsTreeDTO.addChild(assetsTreeChild);
                allAssetsList.addAll(childAssetsDTOList);
            } else {
                childAssetsDTOList.addAll(lowerAssetsList);
                childAssetsDTOList.stream().distinct().collect(Collectors.toList());
                assetsTreeChild.setAssetsList(childAssetsDTOList);
                assetsTreeDTO.addChild(assetsTreeChild);
            }
        }
        allAssetsList = allAssetsList.stream().distinct().collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(assetsTreeDTO.getAssetsList())) {
            assetsTreeDTO.getAssetsList().addAll(allAssetsList);
        } else {
            assetsTreeDTO.setAssetsList(allAssetsList);
        }
        return allAssetsList;
    }

    /**
     * 可视化上传图文区内容
     * @param multipartFile
     * @return
     */
    @Override
    public Reply visualizedImageAndTextAreaUpload(MultipartFile multipartFile) {
        try {
            if (multipartFile.isEmpty()) {
                Reply.fail("文件为空");
            }
            //获取文件名
            String fileName = multipartFile.getOriginalFilename();
            //获取文件的后缀名
            String suffixName = fileName.substring(fileName.lastIndexOf("."));
            String fileNameInTable = UUIDUtils.getUUID() + suffixName;
            File file = new File(new File(backGroudFilePath).getAbsolutePath()
                    + File.separator + File.separator + fileNameInTable);
            //检测是否存在目录
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            multipartFile.transferTo(file);
            Runtime runtime = Runtime.getRuntime();
            return Reply.ok(fileNameInTable);
        }catch (Exception e){
            log.error("上传可视化图文区内容失败,失败信息:",e);
            return Reply.fail("上传可视化图文区内容失败"+e.getMessage());
        }
    }
}
