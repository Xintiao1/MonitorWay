package cn.mw.monitor.visualized.service.impl;

import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.service.assets.api.MwTangibleAssetsService;
import cn.mw.monitor.service.assets.model.MwAssetsLabelDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import cn.mw.monitor.service.assets.param.QueryTangAssetsParam;
import cn.mw.monitor.service.label.api.MwLabelCommonServcie;
import cn.mw.monitor.service.model.dto.PropertyInfo;
import cn.mw.monitor.service.model.param.MwModelInstanceParam;
import cn.mw.monitor.service.model.param.QueryModelInstanceByPropertyIndexParam;
import cn.mw.monitor.service.model.param.QueryModelInstanceByPropertyIndexParamList;
import cn.mw.monitor.service.model.service.MwModelViewCommonService;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.util.IDModelType;
import cn.mw.monitor.util.ModuleIDManager;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.*;
import cn.mw.monitor.visualized.param.MwVisualizedIndexQueryParam;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.param.MwVisualizedZkSoftWareParam;
import cn.mw.monitor.visualized.service.MwVisualizedManageService;
import cn.mw.monitor.visualized.service.impl.manager.*;
import cn.mw.monitor.weixinapi.DelFilter;
import cn.mw.monitor.weixinapi.MessageContext;
import cn.mw.monitor.weixinapi.MwRuleSelectParam;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.BeansUtils;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sun.misc.BASE64Decoder;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName MwVisualizedManageServiceImpl
 * @Author gengjb
 * @Date 2022/4/21 15:07
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedManageServiceImpl implements MwVisualizedManageService {

    @Resource
    private MwVisualizedManageDao manageDao;

    @Autowired
    private MwVisualizedDataSourceManager dataSourceManager;

    @Autowired
    private MwVisualizedDataChangeManager dataChangeManager;

    @Autowired
    MwLabelCommonServcie mwLabelCommonServcie;

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;


    @Autowired
    private MwTangibleAssetsService tangibleAssetsService;

    @Autowired
    private MwVisualizedZkSoftWareManager zkSoftWareManager;

    @Autowired
    private MwZkSoftWareAlertTrendManager alertTrendManager;

    @Autowired
    private MwVisualizedModuleManager moduleManager;

    @Autowired
    private MwModelViewCommonService modelViewCommonService;

    @Value("${model.assets.enable}")
    private boolean modelAssetEnable;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    @Value("${visualized.backgroud}")
    private String backGroudFilePath;

    @Autowired
    private ModuleIDManager idManager;

    /**
     * 添加视图分类
     *
     * @param visualizedClassifyDto 分类信息
     * @return
     */
    @Override
    public Reply addVisualizedClassify(MwVisualizedClassifyDto visualizedClassifyDto) {
        try {
            manageDao.addVisualizedClassify(visualizedClassifyDto);
            return Reply.ok("添加成功");
        } catch (Exception e) {
            log.error("添加可视化视图分类失败,失败信息:", e);
            return Reply.fail("添加可视化视图分类失败" + e.getMessage());
        }
    }


    @Override
    public Reply selectVisualizedClassify() {
        try {
            long startTime = System.currentTimeMillis();
            //查询分类信息
            log.info("MwVisualizedManageServiceImpl {selectVisualizedClassify}:" + startTime);
            List<MwVisualizedClassifyDto> mwVisualizedClassifyDtos = manageDao.selectVisualizedClassify();
            log.info("MwVisualizedManageServiceImpl {selectVisualizedClassify}2:" + (System.currentTimeMillis() - startTime));
            if (CollectionUtils.isEmpty(mwVisualizedClassifyDtos)) return Reply.ok(mwVisualizedClassifyDtos);
            //设置分类下的视图信息
            //查询所有视图
            MwVisualizedViewDto dto = new MwVisualizedViewDto();
            List<MwVisualizedViewDto> mwVisualizedViewDtos = manageDao.selectVisualizedView(dto);
            log.info("MwVisualizedManageServiceImpl {selectVisualizedClassify}3:" + (System.currentTimeMillis() - startTime));
            if (CollectionUtils.isEmpty(mwVisualizedViewDtos)) return Reply.ok(mwVisualizedClassifyDtos);
            //匹配分类信息
            for (MwVisualizedClassifyDto mwVisualizedClassifyDto : mwVisualizedClassifyDtos) {
                mwVisualizedClassifyDto.setUuid(mwVisualizedClassifyDto.getClassifyId());
                mateClassifyData(mwVisualizedClassifyDto, mwVisualizedViewDtos);
            }
            log.info("MwVisualizedManageServiceImpl {selectVisualizedClassify}4:" + (System.currentTimeMillis() - startTime));
            return Reply.ok(mwVisualizedClassifyDtos);
        } catch (Exception e) {
            log.error("查询可视化视图分类失败,失败信息:", e);
            return Reply.fail("查询可视化视图分类失败" + e.getMessage());
        }
    }

    /**
     * 匹配分类下的视图信息
     *
     * @param classifyDto          分类数据
     * @param mwVisualizedViewDtos 视图数据
     */
    private void mateClassifyData(MwVisualizedClassifyDto classifyDto, List<MwVisualizedViewDto> mwVisualizedViewDtos) {
        List<MwVisualizedViewDto> list = new ArrayList<>();
        Integer classifyId = classifyDto.getClassifyId();
        for (MwVisualizedViewDto mwVisualizedViewDto : mwVisualizedViewDtos) {
            mwVisualizedViewDto.setUuid(mwVisualizedViewDto.getId());
            Integer viewClassifyId = mwVisualizedViewDto.getClassifyId();
            if (classifyId != null && viewClassifyId != null && classifyId.equals(viewClassifyId)) {
                //说明该视图是该分类下的数据
                list.add(mwVisualizedViewDto);
            }
        }
        classifyDto.setViews(list);
        classifyDto.setVisualizedCount(list.size());
    }

    /**
     * 修改视图分类
     *
     * @param visualizedClassifyDto 分类信息
     * @return
     */
    @Override
    public Reply updateVisualizedClassify(MwVisualizedClassifyDto visualizedClassifyDto) {
        try {
            manageDao.updateVisualizedClassify(visualizedClassifyDto);
            return Reply.ok("修改成功");
        } catch (Exception e) {
            log.error("修改可视化视图分类失败,失败信息:", e);
            return Reply.fail("修改可视化视图分类失败" + e.getMessage());
        }
    }

    /**
     * 删除视图分类
     *
     * @param visualizedClassifyDto 分类信息
     * @return
     */
    @Override
    public Reply deleteVisualizedClassify(MwVisualizedClassifyDto visualizedClassifyDto) {
        try {
            if (CollectionUtils.isNotEmpty(visualizedClassifyDto.getClassifyIds())) {
                manageDao.deleteVisualizedClassify(visualizedClassifyDto.getClassifyIds());
            }
            List<Integer> viewIds = visualizedClassifyDto.getViewIds();
            if (CollectionUtils.isNotEmpty(viewIds)) {
                manageDao.deleteVisualizedView(viewIds);
            }
            return Reply.ok("删除成功");
        } catch (Exception e) {
            log.error("删除可视化视图分类失败,失败信息:", e);
            return Reply.fail("删除可视化视图分类失败" + e.getMessage());
        }
    }

    /**
     * 添加可视化视图信息
     *
     * @param visualizedViewDto 视图信息
     * @return
     */
    @Override
    public Reply addVisualizedView(MwVisualizedViewDto visualizedViewDto) {
        try {
            if (visualizedViewDto.getId() != null) {
                //走复制功能
                copyVisualizedView(visualizedViewDto.getId());
                return Reply.ok("复制成功");
            }
            //设置创建人，创建时间
            String loginName = iLoginCacheInfo.getLoginName();
            visualizedViewDto.setCreator(loginName);
            visualizedViewDto.setCreateDate(new Date());
            Map visualizedDatas = visualizedViewDto.getVisualizedDatas();
            if (visualizedDatas != null) {
                String s = JSON.toJSONString(visualizedDatas);
                visualizedViewDto.setVisualizedDatasStr(s);
            }
            //Base64编码转为图片
            String fileName = UUID.randomUUID().toString().replace("-", "") + ".jpg";
            String url = backGroudFilePath + "/" + fileName;
            boolean flag = GenerateImage(visualizedViewDto.getVisualizedImage(), url);
            if (flag) {
                visualizedViewDto.setVisualizedImage(fileName);
            }
            manageDao.addVisualizedView(visualizedViewDto);
            return Reply.ok("添加成功");
        } catch (Exception e) {
            log.error("添加可视化视图失败,失败信息:", e);
            return Reply.fail("添加可视化视图失败" + e.getMessage());
        }
    }

    //复制可视化视图
    private void copyVisualizedView(Integer id) {
        MwVisualizedViewDto viewDto = new MwVisualizedViewDto();
        viewDto.setId(id);
        Reply reply = visualizedUpdateQuery(viewDto);
        if (reply == null || reply.getRes() != PaasConstant.RES_SUCCESS) {
            return;
        }
        MwVisualizedViewDto dto = (MwVisualizedViewDto) reply.getData();
        dto.setId(null);
        dto.setVisualizedViewName(dto.getVisualizedViewName() + "_copy");
        manageDao.addVisualizedView(dto);
    }


    /**
     * Base64编码转图片
     *
     * @param imgData
     * @param imgFilePath
     * @return
     * @throws IOException
     */
    public boolean GenerateImage(String imgData, String imgFilePath) throws IOException { // 对字节数组字符串进行Base64解码并生成图片
        File file = new File(new File(backGroudFilePath).getAbsolutePath()
                + File.separator + File.separator + imgFilePath);
        //检测是否存在目录
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (imgData == null) {// 图像数据为空
            return false;
        }
        imgData = imgData.replace(" ", "");
        imgData = imgData.replace("data:image/jpeg;base64,", "");
        BASE64Decoder decoder = new BASE64Decoder();
        OutputStream out = null;
        try {
            out = new FileOutputStream(imgFilePath);
            // Base64解码
            byte[] b = decoder.decodeBuffer(imgData);
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {// 调整异常数据
                    b[i] += 256;
                }
            }
            out.write(b);
        } catch (Throwable e) {
            log.error("上传图片失败", e);
        } finally {
            out.flush();
            out.close();
            return true;
        }
    }


    /**
     * 修改可视化视图信息
     *
     * @param visualizedViewDto 视图信息
     * @return
     */
    @Override
    public Reply updateVisualizedView(MwVisualizedViewDto visualizedViewDto) {
        try {
            String loginName = iLoginCacheInfo.getLoginName();
            visualizedViewDto.setModifier(loginName);
            Map visualizedDatas = visualizedViewDto.getVisualizedDatas();
            if (visualizedDatas != null) {
                String s = JSON.toJSONString(visualizedDatas);
                visualizedViewDto.setVisualizedDatasStr(s);
            }
            //Base64编码转为图片
            String fileName = UUID.randomUUID().toString().replace("-", "") + ".png";
            String url = backGroudFilePath + "/" + fileName;
            GenerateImage(visualizedViewDto.getVisualizedImage(), url);
            visualizedViewDto.setVisualizedImage(fileName);
            manageDao.updateVisualizedView(visualizedViewDto);
            //图片存储
            saveVisualizedImageInfo(visualizedViewDto);
            return Reply.ok("修改成功");
        } catch (Exception e) {
            log.error("修改可视化视图失败,失败信息:", e);
            return Reply.fail("修改可视化视图失败" + e.getMessage());
        }
    }

    /**
     * 可视化修改保存图片信息
     */
    private void saveVisualizedImageInfo(MwVisualizedViewDto visualizedViewDto){
        List<MwVisualizedImageDto> imageDtos = visualizedViewDto.getImageDtos();
        if(CollectionUtils.isEmpty(imageDtos)){return;}
        for (MwVisualizedImageDto imageDto : imageDtos) {
            imageDto.setVisualizedId(visualizedViewDto.getId());
        }
        //删除原来图片信息
        manageDao.deleteVisualizedImageInfo(visualizedViewDto.getId());
        //将数据保存
        manageDao.insertVisualizedImageInfo(imageDtos);
    }

    /**
     * 删除可视化视图信息
     *
     * @param visualizedViewDto 视图信息
     * @return
     */
    @Override
    public Reply deleteVisualizedView(MwVisualizedViewDto visualizedViewDto) {
        try {
            manageDao.deleteVisualizedView(visualizedViewDto.getIds());
            return Reply.ok("删除成功");
        } catch (Exception e) {
            log.error("删除可视化视图失败,失败信息:", e);
            return Reply.fail("删除可视化视图失败" + e.getMessage());
        }
    }


    /**
     * 查询可视化视图数据
     *
     * @return
     */
    @Override
    public Reply selectVisualizedView(MwVisualizedViewDto visualizedViewDto) {
        try {
            PageHelper.startPage(visualizedViewDto.getPageNumber(), visualizedViewDto.getPageSize());
            List<MwVisualizedViewDto> mwVisualizedViewDtos = manageDao.selectVisualizedView(visualizedViewDto);
            if (CollectionUtils.isNotEmpty(mwVisualizedViewDtos)) {
                for (MwVisualizedViewDto mwVisualizedViewDto : mwVisualizedViewDtos) {
                    String visualizedDatasStr = mwVisualizedViewDto.getVisualizedDatasStr();
                    mwVisualizedViewDto.setBackGroundImage(mwVisualizedViewDto.getBackGroundImage());
                    if (StringUtils.isNotBlank(visualizedDatasStr)) {
                        Map map = JSON.parseObject(visualizedDatasStr, Map.class);
                        mwVisualizedViewDto.setVisualizedDatas(map);
                    }
                }
            }
            PageInfo pageInfo = new PageInfo<>(mwVisualizedViewDtos);
            pageInfo.setList(mwVisualizedViewDtos);
            return Reply.ok(pageInfo);
        } catch (Exception e) {
            log.error("查询可视化视图失败,失败信息:", e);
            return Reply.fail("查询可视化视图失败" + e.getMessage());
        }
    }


    /**
     * 查询指标数据
     *
     * @param param
     * @return
     */
    @Override
    public Reply queryVisualizedItem(MwVisualizedIndexQueryParam param) {
        try {
            if (param.getDateType() == null && param.getType() == null
                    && StringUtils.isBlank(param.getStartTime()) && StringUtils.isBlank(param.getEndTime())) {
                return Reply.fail("时间不能为空");
            }
            //将资产数据与过滤规则进行匹配
            List<String> hostIds = assetsMateFilterRule(param);
            //需查询的指标数据
            List<MwVisualizedIndexDto> indexDtos = param.getIndexDtos();
            List<MwVisualizedIndexDto> mwVisualizedIndexDtos = new ArrayList<>();
            for (MwVisualizedIndexDto indexDto : indexDtos) {
                String assetsId = indexDto.getAssetsId();
                if (hostIds.contains(assetsId)) {
                    mwVisualizedIndexDtos.add(indexDto);
                }
            }
            param.setDataSource(1);
            param.setIndexDtos(mwVisualizedIndexDtos);
            List<MwVisualizedZabbixDataDto> zabbixDataDtos = (List<MwVisualizedZabbixDataDto>) dataSourceManager.getDataByType(param.getDataSource(), param);
            Object realData = dataChangeManager.getDataByType(param.getChartType(), zabbixDataDtos);
            return Reply.ok(realData);
        } catch (Exception e) {
            log.error("查询可视化指标明细失败,失败信息:", e);
            return Reply.fail("查询可视化指标明细失败" + e.getMessage());
        }
    }


    private List<MwRuleSelectParam> handleFilterRule(List<MwRuleSelectParam> ruleSelectList) {
        List<MwRuleSelectParam> paramList = new ArrayList<>();
        if (CollectionUtils.isEmpty(ruleSelectList)) return paramList;
        for (MwRuleSelectParam s : ruleSelectList) {
            MwRuleSelectParam ruleSelectDto = new MwRuleSelectParam();
            ruleSelectDto.setCondition(s.getCondition());
            ruleSelectDto.setDeep(s.getDeep());
            ruleSelectDto.setKey(s.getKey());
            ruleSelectDto.setName(s.getName());
            ruleSelectDto.setParentKey(s.getParentKey());
            ruleSelectDto.setRelation(s.getRelation());
            ruleSelectDto.setValue(s.getValue());
            paramList.add(ruleSelectDto);
            if (s.getConstituentElements() != null && s.getConstituentElements().size() > 0) {
                paramList.addAll(delMwRuleSelectList(s));
            }
        }
        return paramList;
    }


    public List<MwRuleSelectParam> delMwRuleSelectList(MwRuleSelectParam param) {
        List<MwRuleSelectParam> paramList = new ArrayList<>();
        for (MwRuleSelectParam s : param.getConstituentElements()) {
            MwRuleSelectParam ruleSelectDto = new MwRuleSelectParam();
            ruleSelectDto.setCondition(s.getCondition());
            ruleSelectDto.setDeep(s.getDeep());
            ruleSelectDto.setKey(s.getKey());
            ruleSelectDto.setName(s.getName());
            ruleSelectDto.setParentKey(s.getParentKey());
            ruleSelectDto.setRelation(s.getRelation());
            ruleSelectDto.setValue(s.getValue());
            ruleSelectDto.setUuid(param.getUuid());
            paramList.add(ruleSelectDto);
            s.setUuid(param.getUuid());
            if (s.getConstituentElements() != null && s.getConstituentElements().size() > 0) {
                List<MwRuleSelectParam> temps = delMwRuleSelectList(s);
                paramList.addAll(temps);
            }
        }
        return paramList;
    }


    /**
     * 资产匹配过滤规则
     *
     * @param param
     */
    private List<String> assetsMateFilterRule(MwVisualizedIndexQueryParam param) {
        List<String> assetsIds = param.getAssetsIds();
        List<MwRuleSelectParam> selectListParam = param.getMwRuleSelectListParam();
        if (CollectionUtils.isEmpty(assetsIds) || CollectionUtils.isEmpty(selectListParam)) return assetsIds;
        List<MwRuleSelectParam> ruleSelectList = handleFilterRule(selectListParam);
        if (ruleSelectList.size() <= 2) return assetsIds;
        List<String> filterAssetsIds = new ArrayList<>();
        //查询资产信息
        QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
        assetsParam.setPageNumber(1);
        assetsParam.setPageSize(Integer.MAX_VALUE);
        //根据类型查询资产数据
        Reply reply = tangibleAssetsService.selectList(assetsParam);
        List<MwTangibleassetsTable> mwTangibleassetsDTOS = new ArrayList<>();
        Object data = reply.getData();
        PageInfo newPageInfo = new PageInfo<>();
        if (null != data) {
            PageInfo pageInfo = (PageInfo) data;
            mwTangibleassetsDTOS = pageInfo.getList();
        }
        HashMap<String, Object> assetsMap = new HashMap<>();
        for (MwTangibleassetsTable mwTangibleassetsDTO : mwTangibleassetsDTOS) {
            String monitorServerName = mwTangibleassetsDTO.getMonitorServerName();
            String assetsName = mwTangibleassetsDTO.getAssetsName();
            String assetsTypeName = mwTangibleassetsDTO.getAssetsTypeName();
            List<MwAssetsLabelDTO> labelList = mwLabelCommonServcie.getLabelBoard(mwTangibleassetsDTO.getId(), "ASSETS");
            List<String> labelValue = new ArrayList<>();
            if (labelList != null && labelList.size() > 0) {
                for (MwAssetsLabelDTO s : labelList) {
                    if (s.getTagboard() != null) {
                        labelValue.add(s.getTagboard());
                    }
                    if (s.getDropValue() != null) {
                        labelValue.add(s.getDropValue());
                    }
                    if (s.getDateTagboard() != null) {
                        labelValue.add(s.getDateTagboard().toString());
                    }
                }
            }
            assetsMap.put("数据来源", monitorServerName);
            assetsMap.put("资产", assetsName);
            assetsMap.put("资产类型", assetsTypeName);
            assetsMap.put("标签", labelValue);
            MessageContext messageContext = new MessageContext();
            messageContext.setKey(assetsMap);
            Boolean resultBoolean = true;
            if (ruleSelectList.size() > 2) {
                log.info("ruleSelectParams star");
                List<MwRuleSelectParam> ruleSelectParams = new ArrayList<>();
                for (MwRuleSelectParam s : ruleSelectList) {
                    if (s.getParentKey().equals("root")) {
                        ruleSelectParams.add(s);
                    }
                }
                for (MwRuleSelectParam s : ruleSelectParams) {
                    s.setConstituentElements(getChild(s.getKey(), ruleSelectList));
                }
                resultBoolean = DelFilter.delFilter(ruleSelectParams, messageContext, ruleSelectList);
                if (resultBoolean) {
                    filterAssetsIds.add(mwTangibleassetsDTO.getAssetsId());
                }
            }
        }
        return filterAssetsIds;
    }

    private static List<MwRuleSelectParam> getChild(String key, List<MwRuleSelectParam> rootList) {
        List<MwRuleSelectParam> childList = new ArrayList<>();
        for (MwRuleSelectParam s : rootList) {
            if (s.getParentKey().equals(key)) {
                childList.add(s);
            }
        }
        for (MwRuleSelectParam s : childList) {
            s.setConstituentElements(getChild(s.getKey(), rootList));
        }
        if (childList.size() == 0) {
            return null;
        }
        return childList;
    }

    /**
     * 可视化点击渲染数据
     *
     * @param viewDto
     * @return
     */
    @Override
    public Reply visualizedUpdateQuery(MwVisualizedViewDto viewDto) {
        try {
            Integer id = viewDto.getId();
            //根据ID查询可视化数据
            MwVisualizedViewDto dto = manageDao.selectVisualizedById(id);
            String visualizedDatasStr = dto.getVisualizedDatasStr();
            //将字符串转为JSON
            if (StringUtils.isBlank(visualizedDatasStr)) return Reply.ok(dto);
            Map map = JSON.parseObject(visualizedDatasStr, Map.class);
            List dataList = (List) map.get("nodes");
            if (CollectionUtils.isNotEmpty(dataList)) {
                for (Object o2 : dataList) {
                    Map<String, Object> map2 = (Map<String, Object>) o2;
                    Map renderData = (Map) map2.get("renderData");
                    JSONObject jsonObject = (JSONObject) renderData.get("query");
                    if (jsonObject == null) continue;
                    MwVisualizedIndexQueryParam indexQueryParam = JSON.parseObject(jsonObject.toJSONString(), MwVisualizedIndexQueryParam.class);
                    indexQueryParam.setIsExport(viewDto.getIsExport());
                    //查询数据
                    Reply reply = queryVisualizedItem(indexQueryParam);
                    if (null != reply && reply.getRes() == PaasConstant.RES_SUCCESS) {
                        Object data = reply.getData();
                        renderData.put("render", data);
                    }
                }
            }
            dto.setVisualizedDatas(map);
            dto.setBackGroundImage(viewDto.getBackGroundImage());
            //获取图片信息
            List<MwVisualizedImageDto> imageDtos = manageDao.selectVisualizedImageInfo(dto.getId());
            dto.setImageDtos(imageDtos);
            return Reply.ok(dto);
        } catch (Throwable e) {
            log.error("编辑前查询可视化视图失败,失败信息:", e);
            return Reply.fail("编辑前查询可视化视图失败" + e.getMessage());
        }
    }

    @Override
    public Reply saveVisualizedQueryValue(MwVisualizedIndexQueryParam viewDto) {
        int id = 0;
        try {
            MwVisualizedQueryValueDTO dto = new MwVisualizedQueryValueDTO();
            if (viewDto != null) {
                String jsonString = JSONObject.toJSONString(viewDto);
                dto.setQueryValueJsonStr(jsonString);
                manageDao.saveVisualizedQueryValue(dto);
                id = dto.getId();
            }
        } catch (Throwable e) {
            log.error("saveVisualizedQueryValue to fail:", e);
            return Reply.fail("保存失败" + e.getMessage());
        }
        return Reply.ok(id);
    }

    @Override
    public Reply getVisualizedQueryValue(Integer id) {
        try {
            MwVisualizedQueryValueDTO dto = manageDao.getVisualizedQueryValue(id);
            MwVisualizedIndexQueryParam viewDto = new MwVisualizedIndexQueryParam();
            if (dto != null && dto.getQueryValueJsonStr() != null) {
                viewDto = JSONObject.parseObject(dto.getQueryValueJsonStr(), MwVisualizedIndexQueryParam.class);
            }
            return Reply.ok(viewDto);
        } catch (Throwable e) {
            log.error("getVisualizedQueryValue to fail:", e);
            return Reply.fail("查询失败" + e.getMessage());
        }
    }


    @Override
    public Reply selectVisualizedZkSoftWare(MwVisualizedZkSoftWareParam param) {
        Object realData = zkSoftWareManager.getDataByType(param.getChartType());
        return Reply.ok(realData);
    }


    @Override
    public Reply selectVisualizedZkSoftWareAlertTrend(MwVisualizedZkSoftWareParam param) {
        return Reply.ok(alertTrendManager.getAlertTrend(param));
    }

    @Override
    public Reply selectVisualizedModule(MwVisualizedModuleParam param) {
        return Reply.ok(moduleManager.getDataByType(param.getChartType(), param));
    }

    /**
     * 获取模型实例数据
     *
     * @param moduleParam
     */
    @Override
    public List<MwTangibleassetsDTO> getModelAssets(MwVisualizedModuleParam moduleParam, Boolean queryAssetsState) throws Exception {
        if (modelAssetEnable) {
            QueryModelInstanceByPropertyIndexParamList indexParamList = new QueryModelInstanceByPropertyIndexParamList();
            indexParamList.setIsQueryAssetsState(queryAssetsState);
            indexParamList.setSkipDataPermission(true);
            if (StringUtils.isBlank(moduleParam.getPropertiesIndexId()) && moduleParam.getModelInstanceId() == null && StringUtils.isBlank(moduleParam.getClassIfyPropertiesIndexId()) && moduleParam.getClassIfyModelInstanceId() == null && CollectionUtils.isEmpty(moduleParam.getPropertyIndexParams())) {
                indexParamList.setParamLists(null);
                return modelViewCommonService.findModelAssetsByRelationIds(indexParamList);
            }
            List<QueryModelInstanceByPropertyIndexParam> paramLists = new ArrayList<>();
            if (StringUtils.isNotBlank(moduleParam.getPropertiesIndexId()) && moduleParam.getModelInstanceId() != null) {
                QueryModelInstanceByPropertyIndexParam propertyIndexParam = new QueryModelInstanceByPropertyIndexParam();
                propertyIndexParam.setPropertiesIndexId(moduleParam.getPropertiesIndexId());
                propertyIndexParam.setPropertiesValue(String.valueOf(moduleParam.getModelInstanceId()));
                paramLists.add(propertyIndexParam);
            }
            if (StringUtils.isNotBlank(moduleParam.getClassIfyPropertiesIndexId()) && moduleParam.getClassIfyModelInstanceId() != null) {
                QueryModelInstanceByPropertyIndexParam propertyIndexParam = new QueryModelInstanceByPropertyIndexParam();
                propertyIndexParam.setPropertiesIndexId(moduleParam.getClassIfyPropertiesIndexId());
                propertyIndexParam.setPropertiesValue(String.valueOf(moduleParam.getClassIfyModelInstanceId()));
                paramLists.add(propertyIndexParam);
            }
            if (CollectionUtils.isNotEmpty(moduleParam.getPropertyIndexParams())) {
                paramLists.addAll(moduleParam.getPropertyIndexParams());
            }

            indexParamList.setParamLists(paramLists);
            log.info("可视化查询资产数据资源中心" + indexParamList);
            List<MwTangibleassetsDTO> tangibleassetsDTOS = modelViewCommonService.findModelAssetsByRelationIds(indexParamList);
            if (moduleParam.getIsFilterMonitorFlag() != null && moduleParam.getIsFilterMonitorFlag()) {
                return tangibleassetsDTOS;
            }
            if (CollectionUtils.isNotEmpty(tangibleassetsDTOS)) {
                List<MwTangibleassetsDTO> collect = tangibleassetsDTOS.stream().filter(item -> item.getMonitorFlag() != null && item.getMonitorFlag() == true).collect(Collectors.toList());
                return collect;
            }
            return modelViewCommonService.findModelAssetsByRelationIds(indexParamList);
        }
        List<MwTangibleassetsDTO> mwTangibleassetsDTOS = new ArrayList<>();
        QueryTangAssetsParam assetsParam = new QueryTangAssetsParam();
        assetsParam.setPageNumber(1);
        assetsParam.setPageSize(Integer.MAX_VALUE);
        assetsParam.setIsQueryAssetsState(queryAssetsState);
        assetsParam.setUserId(iLoginCacheInfo.getCacheInfo(iLoginCacheInfo.getLoginName()).getUserId());
        List<MwTangibleassetsTable> mwTangibleassetsTables = mwAssetsManager.getAssetsTable(assetsParam);
        if (CollectionUtils.isNotEmpty(mwTangibleassetsTables)) {
            for (MwTangibleassetsTable mwTangibleassetsTable : mwTangibleassetsTables) {
                MwTangibleassetsDTO mwTangibleassetsDTO = new MwTangibleassetsDTO();
                BeansUtils.copyProperties(mwTangibleassetsTable, mwTangibleassetsDTO);
                mwTangibleassetsDTOS.add(mwTangibleassetsDTO);
            }
        }
        return mwTangibleassetsDTOS;
    }

    /**
     * 获取选择业务信息
     *
     * @return
     */
    @Override
    public Reply getBusinessTreeInfo() {
        try {
            List<MwVisualizedModuleBusinessTreeDto> moduleBusinessTreeDtos = new ArrayList<>();
            if (!modelAssetEnable) {
                return Reply.ok(moduleBusinessTreeDtos);
            }
            Reply systemAndClassify = modelViewCommonService.getModelSystemAndClassify();
            if (systemAndClassify == null || systemAndClassify.getRes() != PaasConstant.RES_SUCCESS) {
                return Reply.ok(moduleBusinessTreeDtos);
            }
            List<PropertyInfo> infos = (List<PropertyInfo>) systemAndClassify.getData();
            Reply instanceInfo = modelViewCommonService.getSystemAndClassifyInstanceInfo();
            if (instanceInfo == null || instanceInfo.getRes() != PaasConstant.RES_SUCCESS) {
                return Reply.ok(moduleBusinessTreeDtos);
            }
            Map<String, List<MwModelInstanceParam>> map = (Map<String, List<MwModelInstanceParam>>) instanceInfo.getData();
            for (PropertyInfo info : infos) {
                MwVisualizedModuleBusinessTreeDto businessTreeDto = new MwVisualizedModuleBusinessTreeDto();
                businessTreeDto.setName(info.getPropertiesName());
                businessTreeDto.setIndexId(info.getIndexId());
                businessTreeDto.setDisabled(false);
                businessTreeDto.setUuid(UUID.randomUUID().toString().replace("-", ""));
                List<MwModelInstanceParam> mwModelInstanceParams = map.get(info.getIndexId());
                if (CollectionUtils.isEmpty(mwModelInstanceParams)) {
                    continue;
                }
                List<MwVisualizedModuleBusinessTreeDto> children = getBusinessTreeChildren(mwModelInstanceParams, info.getIndexId());
                businessTreeDto.setChildren(children);
                moduleBusinessTreeDtos.add(businessTreeDto);
            }
            return Reply.ok(moduleBusinessTreeDtos);
        } catch (Throwable e) {
            log.error("查询业务绑定信息数据失败", e);
            return Reply.fail("查询业务绑定信息数据失败" + e.getMessage());
        }
    }

    /**
     * 获取子业务
     *
     * @param mwModelInstanceParams
     */
    private List<MwVisualizedModuleBusinessTreeDto> getBusinessTreeChildren(List<MwModelInstanceParam> mwModelInstanceParams, String indexId) {
        List<MwVisualizedModuleBusinessTreeDto> children = new ArrayList<>();
        for (MwModelInstanceParam mwModelInstanceParam : mwModelInstanceParams) {
            MwVisualizedModuleBusinessTreeDto treeDto = new MwVisualizedModuleBusinessTreeDto();
            treeDto.setName(mwModelInstanceParam.getInstanceName());
            treeDto.setIndexId(indexId);
            treeDto.setTypeId(mwModelInstanceParam.getInstanceId());
            treeDto.setDisabled(true);
            treeDto.setUuid(UUID.randomUUID().toString().replace("-", ""));
            children.add(treeDto);
        }
        return children;
    }

    @Override
    public Reply getAssetsTypeGroup(MwVisualizedModuleParam moduleParam) {
        try {
            List<MwVisualizedModuleBusinessTreeDto> moduleBusinessTreeDtos = new ArrayList<>();
            List<MwTangibleassetsDTO> modelAssets = getModelAssets(moduleParam, false);
            if (CollectionUtils.isEmpty(modelAssets)) {
                return Reply.ok(null);
            }
            Map<String, List<MwTangibleassetsDTO>> assetsMap = modelAssets.stream().collect(Collectors.groupingBy(item -> item.getAssetsTypeSubName()));
            if (CollectionUtils.isEmpty(assetsMap)) {
                return Reply.ok(assetsMap);
            }
            for (String typeName : assetsMap.keySet()) {
                MwVisualizedModuleBusinessTreeDto businessTreeDto = new MwVisualizedModuleBusinessTreeDto();
                businessTreeDto.setName(typeName);
                businessTreeDto.setDisabled(true);
                businessTreeDto.setUuid(UUID.randomUUID().toString().replace("-", ""));
                List<MwVisualizedModuleBusinessTreeDto> children = new ArrayList<>();
                List<MwTangibleassetsDTO> mwTangibleassetsDTOS = assetsMap.get(typeName);
                if (CollectionUtils.isEmpty(mwTangibleassetsDTOS)) {
                    continue;
                }
                for (MwTangibleassetsDTO mwTangibleassetsDTO : mwTangibleassetsDTOS) {
                    MwVisualizedModuleBusinessTreeDto childrenBusinessTreeDto = new MwVisualizedModuleBusinessTreeDto();
                    childrenBusinessTreeDto.setName(mwTangibleassetsDTO.getAssetsName() == null ? mwTangibleassetsDTO.getInstanceName() : mwTangibleassetsDTO.getAssetsName());
                    childrenBusinessTreeDto.setId(mwTangibleassetsDTO.getId() == null ? String.valueOf(mwTangibleassetsDTO.getModelInstanceId()) : mwTangibleassetsDTO.getId());
                    childrenBusinessTreeDto.setDisabled(false);
                    childrenBusinessTreeDto.setUuid(UUID.randomUUID().toString().replace("-", ""));
                    children.add(childrenBusinessTreeDto);
                }
                businessTreeDto.setChildren(children);
                moduleBusinessTreeDtos.add(businessTreeDto);
            }
            return Reply.ok(moduleBusinessTreeDtos);
        } catch (Exception e) {
            log.error("查询可视化资产类型分组失败,失败信息:", e);
            return Reply.fail("查询可视化资产类型分组失败" + e.getMessage());
        }
    }

    @Override
    public Reply gettVisualizedDropDownInfo(MwVisualizedModuleParam moduleParam) {
        try {
            List<MwVisualizedDropDownDto> mwVisualizedDropDownDtos = manageDao.selectVisualizedDropDownInfo(moduleParam.getType());
            return Reply.ok(mwVisualizedDropDownDtos);
        } catch (Exception e) {
            log.error("查询可视化下拉信息失败,失败信息:", e);
            return Reply.fail("查询可视化下拉信息失败" + e.getMessage());
        }
    }

    /**
     * 获取容器选择接口
     *
     * @param moduleParam
     * @return
     */
    @Override
    public Reply getVisualizedContaineDropDown(MwVisualizedModuleParam moduleParam) {
        try {
            List<MwVisualizedPrometheusDropDto> prometheusDropDtos = manageDao.selectVisualizedContaine(moduleParam.getTypeName());
            return Reply.ok(prometheusDropDtos);
        } catch (Exception e) {
            log.error("查询可视化容器信息失败,失败信息:", e);
            return Reply.fail("查询可视化容器信息失败" + e.getMessage());
        }
    }

    @Override
    public Reply createVisualizedBusinStatusTitle(List<MwVisualizedModuleBusinSatusDto> businSatusDtos) {
        try {
            if (CollectionUtils.isEmpty(businSatusDtos)) {
                return Reply.ok("新增成功");
            }
            for (MwVisualizedModuleBusinSatusDto businSatusDto : businSatusDtos) {
                businSatusDto.setId(String.valueOf(idManager.getID(IDModelType.Visualized)));
            }
            manageDao.insertVisualizedBusinStatusTitle(businSatusDtos);
            return Reply.ok("新增成功");
        } catch (Exception e) {
            log.error("创建可视化业务状态标题分区信息失败,失败信息:", e);
            return Reply.fail("创建可视化业务状态标题分区信息失败" + e.getMessage());
        }
    }

    @Override
    public Reply selectVisualizedBusinStatusTitle(MwVisualizedModuleBusinSatusDto businSatusDto) {
        try {
            List<MwVisualizedModuleBusinSatusDto> mwVisualizedModuleBusinSatusDtos = manageDao.selectVisualizedBusinStatusTitle(businSatusDto.getModelSystemName());
            return Reply.ok(mwVisualizedModuleBusinSatusDtos);
        } catch (Exception e) {
            log.error("查询可视化业务状态标题分区信息失败,失败信息:", e);
            return Reply.fail("查询可视化业务状态标题分区信息失败" + e.getMessage());
        }
    }


    @Override
    public Reply selectVisualizedBusinStatusDropDown() {
        try {
            List<MwVisualizedHostGroupDto> visualizedHostGroupDtos = manageDao.selectHostAndGroupCache(null, null);
            Map<String, List<String>> groupMap = new HashMap<>();
            if (CollectionUtils.isEmpty(visualizedHostGroupDtos)) {
                return Reply.ok(groupMap);
            }
            //按照名称分组
            for (MwVisualizedHostGroupDto visualizedHostGroupDto : visualizedHostGroupDtos) {
                String name = "";
                if (StringUtils.isBlank(visualizedHostGroupDto.getHostGroupName()) || !visualizedHostGroupDto.getHostGroupName().contains("_")) {
                    name = visualizedHostGroupDto.getHostGroupName();
                    visualizedHostGroupDto.setHostGroupName(name);
                    continue;
                }
                String[] groupNames = visualizedHostGroupDto.getHostGroupName().split("_", 2);
                if (groupNames.length > 1) {
                    name = groupNames[1];
                    visualizedHostGroupDto.setHostGroupName(name);
                    continue;
                }
                name = visualizedHostGroupDto.getHostGroupName();
                visualizedHostGroupDto.setHostGroupName(name);
            }
            Map<String, List<MwVisualizedHostGroupDto>> listMap = visualizedHostGroupDtos.stream().collect(Collectors.groupingBy(item -> item.getServerName()));
            for (String serverName : listMap.keySet()) {
                List<MwVisualizedHostGroupDto> hostGroupDtos = listMap.get(serverName);
                Set<String> set = new HashSet<>();
                for (MwVisualizedHostGroupDto hostGroupDto : hostGroupDtos) {
                    set.add(hostGroupDto.getHostGroupName());
                }
                groupMap.put(serverName, new ArrayList<>(set));
            }
            return Reply.ok(groupMap);
        } catch (Exception e) {
            log.error("查询业务状态下拉失败,失败信息:", e);
            return Reply.fail("查询业务状态下拉失败" + e.getMessage());
        }
    }

}
