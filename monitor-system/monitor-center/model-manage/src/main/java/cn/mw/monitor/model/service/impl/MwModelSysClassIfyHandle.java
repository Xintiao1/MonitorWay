package cn.mw.monitor.model.service.impl;

import cn.mw.monitor.model.dao.MwModelInstanceDao;
import cn.mw.monitor.model.dao.MwModelManageDao;
import cn.mw.monitor.model.dto.MwModelViewTreeDTO;
import cn.mw.monitor.model.param.MwModelTangibleAssetsDTO;
import cn.mw.monitor.model.param.QueryInstanceParam;
import cn.mw.monitor.service.model.dto.ModelInfo;
import cn.mwpaas.common.utils.CollectionUtils;
import cn.mwpaas.common.utils.StringUtils;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.mw.monitor.service.model.util.ValConvertUtil.intValueConvert;

/**
 * @ClassName MwModelSysClassIfyHandle
 * @Description 模型资产视图业务系统处理
 * @Author gengjb
 * @Date 2023/4/6 15:54
 * @Version 1.0
 **/
@Component
@Slf4j
public class MwModelSysClassIfyHandle {

    private final String UNKNOWN = "未知";
    private final Pattern pattern = Pattern.compile("^[+]?[\\d]*$");

    @Resource
    private MwModelManageDao mwModelManageDao;

    @Value("${model.tree.classify.enable}")
    private boolean classifyEnable;
    @Value("${modelSystem.ModelId}")
    private Integer modelSystemModelId;
    @Resource
    private MwModelInstanceDao mwModelInstanceDao;
    /**
     * 处理业务系统树结构数据
     *
     * @param modelAssetsDtos 资产视图实例数据
     * @return 返回树结构数据
     */
    public List<MwModelViewTreeDTO> handleSysClassIfy(List<MwModelTangibleAssetsDTO> modelAssetsDtos) {
        if (CollectionUtils.isEmpty(modelAssetsDtos)) {
            return new ArrayList<>();
        }
        List<MwModelViewTreeDTO> treeDTO = new ArrayList<>();
        //获取所有资产类型并分组
        Map<Integer, ModelInfo> assetsSubTypeGroup = getAssetsSubTypeGroup();
//        //获取业务系统名称
        getModelSysClassify(modelAssetsDtos);
        //根据业务系统字段进行分组
        Map<String, List<MwModelTangibleAssetsDTO>> modelSys = modelAssetsDtos.stream().collect(Collectors.groupingBy(item -> !Strings.isNullOrEmpty(item.getModelSystem()) ? item.getModelSystem() : UNKNOWN));
        List<String> systemModelNames = mwModelInstanceDao.getInstanceNameByModelId(modelSystemModelId);
        for(String name : systemModelNames){
            if(!modelSys.containsKey(name)){
                modelSys.put(name,new ArrayList<>());
            }
        }
        //所有数据为一级分类
        if (modelSys == null || modelSys.isEmpty()) {
            return treeDTO;
        }
        for (Map.Entry<String, List<MwModelTangibleAssetsDTO>> entry : modelSys.entrySet()) {
            String id = UUID.randomUUID().toString().replace("-", "");
            MwModelViewTreeDTO viewTreeDTO = new MwModelViewTreeDTO();
            String modelSysName = entry.getKey();//业务系统名称
            List<MwModelTangibleAssetsDTO> assetsDTOS = entry.getValue();
            if (StringUtils.isBlank(modelSysName) || modelSysName.equals(UNKNOWN) || pattern.matcher(modelSysName).matches()) {
                viewTreeDTO.setName(UNKNOWN);
            } else {
//                if(sysClassifyMap != null && sysClassifyMap.size()>0 && sysClassifyMap.containsKey(intValueConvert(modelSysName))){
                    viewTreeDTO.setName(modelSysName);
//                    viewTreeDTO.setUrl(sysClassifyMap.get(Integer.parseInt(modelSysName)).getUrl());
//                    viewTreeDTO.setCustomFlag(sysClassifyMap.get(Integer.parseInt(modelSysName)).getCustomFlag());
//                }

            }
            viewTreeDTO.setId(id);
            viewTreeDTO.setPId(String.valueOf(-1));
            if (CollectionUtils.isEmpty(assetsDTOS)) {
                viewTreeDTO.setInstanceNum(0);
                //如果为空，实例ID集合传入-1，避免前端进行判断处理
                viewTreeDTO.setInstanceIds(Arrays.asList(-1));
                treeDTO.add(viewTreeDTO);
                continue;
            }
            viewTreeDTO.setInstanceIds(assetsDTOS.stream().map(item -> item.getModelInstanceId()).collect(Collectors.toList()));
            viewTreeDTO.setInstanceNum(assetsDTOS.size());
            if(!classifyEnable){
                treeDTO.add(viewTreeDTO);
                continue;
            }
            //根据业务分类进行二级分类处理
            Map<String, List<MwModelTangibleAssetsDTO>> classAssetsDtos = assetsDTOS.stream().collect(Collectors.groupingBy(item -> !Strings.isNullOrEmpty(item.getModelClassify()) ? item.getModelClassify() : UNKNOWN));
            if (classAssetsDtos == null || classAssetsDtos.isEmpty()) {
                continue;
            }
            handleTowLevelClassify(classAssetsDtos, treeDTO, id, assetsSubTypeGroup);
            treeDTO.add(viewTreeDTO);
        }
        return treeDTO;
    }

    /**
     * 处理业务系统二级分类
     *
     * @param classAssetsDtos
     */
    private void handleTowLevelClassify(Map<String, List<MwModelTangibleAssetsDTO>> classAssetsDtos, List<MwModelViewTreeDTO> treeDTO, String pid, Map<Integer, ModelInfo> assetsSubTypeGroup) {
        for (Map.Entry<String, List<MwModelTangibleAssetsDTO>> entry : classAssetsDtos.entrySet()) {
            String id = UUID.randomUUID().toString().replace("-", "");
            MwModelViewTreeDTO viewTreeDTO = new MwModelViewTreeDTO();
            String modelClassifyName = entry.getKey();//业务分类名称
            List<MwModelTangibleAssetsDTO> assetsDTOS = entry.getValue();
            if (StringUtils.isBlank(modelClassifyName) || modelClassifyName.equals(UNKNOWN) || pattern.matcher(modelClassifyName).matches()) {
                viewTreeDTO.setName(UNKNOWN);
            } else {
//                if(sysClassifyMap == null || sysClassifyMap.isEmpty() || !sysClassifyMap.containsKey(Integer.parseInt(modelClassifyName))){continue;}
                viewTreeDTO.setName(modelClassifyName);
//                viewTreeDTO.setUrl(sysClassifyMap.get(Integer.parseInt(modelClassifyName)).getUrl());
//                viewTreeDTO.setCustomFlag(sysClassifyMap.get(Integer.parseInt(modelClassifyName)).getCustomFlag());
            }
            viewTreeDTO.setId(id);
            viewTreeDTO.setPId(pid);
            if (CollectionUtils.isEmpty(assetsDTOS)) {
                viewTreeDTO.setInstanceNum(0);
                //如果为空，实例ID集合传入-1，避免前端进行判断处理
                viewTreeDTO.setInstanceIds(Arrays.asList(-1));
                treeDTO.add(viewTreeDTO);
                continue;
            }
            viewTreeDTO.setInstanceIds(assetsDTOS.stream().map(item -> item.getModelInstanceId()).collect(Collectors.toList()));
            viewTreeDTO.setInstanceNum(assetsDTOS.size());
            //资产子类型分类
            Map<Integer, List<MwModelTangibleAssetsDTO>> assetsSubTypeClassify = assetsDTOS.stream().collect(Collectors.groupingBy(item -> item.getAssetsTypeSubId() != null ? item.getAssetsTypeSubId() : 0));
            handleAssetsTypeClassify(assetsSubTypeClassify, treeDTO, id, assetsSubTypeGroup);
            treeDTO.add(viewTreeDTO);
        }
    }

    /**
     * 根据模型资产子类型进行数据分类
     *
     * @param assetsSubTypeClassify
     * @param treeDTO
     * @param pid
     */
    private void handleAssetsTypeClassify(Map<Integer, List<MwModelTangibleAssetsDTO>> assetsSubTypeClassify, List<MwModelViewTreeDTO> treeDTO, String pid, Map<Integer, ModelInfo> assetsSubTypeGroup) {
        for (Map.Entry<Integer, List<MwModelTangibleAssetsDTO>> entry : assetsSubTypeClassify.entrySet()) {
            String id = UUID.randomUUID().toString().replace("-", "");
            MwModelViewTreeDTO viewTreeDTO = new MwModelViewTreeDTO();
            Integer assetsSubTypeId = entry.getKey();//资产子类型ID
            ModelInfo modelInfo = assetsSubTypeGroup.get(assetsSubTypeId);
            List<MwModelTangibleAssetsDTO> assetsDTOS = entry.getValue();
            if (modelInfo == null || StringUtils.isBlank(modelInfo.getModelName())) {
                viewTreeDTO.setName(UNKNOWN);
            } else {
//                viewTreeDTO.setUrl(modelInfo.getModelIcon());
                viewTreeDTO.setName(modelInfo.getModelName());
//                viewTreeDTO.setCustomFlag(modelInfo.getIconType());

            }
            viewTreeDTO.setId(id);
            viewTreeDTO.setPId(pid);
            if (CollectionUtils.isEmpty(assetsDTOS)) {
                viewTreeDTO.setInstanceNum(0);
                //如果为空，实例ID集合传入-1，避免前端进行判断处理
                viewTreeDTO.setInstanceIds(Arrays.asList(-1));
                treeDTO.add(viewTreeDTO);
                continue;
            }
            viewTreeDTO.setInstanceIds(assetsDTOS.stream().map(item -> item.getModelInstanceId()).collect(Collectors.toList()));
            viewTreeDTO.setInstanceNum(assetsDTOS.size());
            treeDTO.add(viewTreeDTO);
        }

    }

    /**
     * 获取资产子类型
     */
    private Map<Integer, ModelInfo> getAssetsSubTypeGroup() {
        Map<Integer, ModelInfo> subTypeGroupMap = new HashMap<>();
        List<ModelInfo> modelInfoList = mwModelManageDao.getBaseModelInfos();
        if (CollectionUtils.isEmpty(modelInfoList)) {
            return subTypeGroupMap;
        }
        subTypeGroupMap = modelInfoList.stream().collect(Collectors.toMap(ModelInfo::getModelId, Function.identity()));
        return subTypeGroupMap;
    }

    /**
     * 获取模型业务系统对应数据
     */
    private List<MwModelTangibleAssetsDTO> getModelSysClassify(List<MwModelTangibleAssetsDTO> modelAssetsDtos) {
//        Map<Integer, QueryInstanceParam> instanceParamMap = new HashMap<>();
//        Set<Integer> modelIds = new HashSet<>();
        for (MwModelTangibleAssetsDTO modelAssetsDto : modelAssetsDtos) {
            if (StringUtils.isNotBlank(modelAssetsDto.getModelSystem()) && pattern.matcher(modelAssetsDto.getModelSystem()).matches()) {
                modelAssetsDto.setModelSystem(UNKNOWN);
            }
            if (StringUtils.isNotBlank(modelAssetsDto.getModelClassify()) && pattern.matcher(modelAssetsDto.getModelClassify()).matches()) {
                modelAssetsDto.setModelClassify(UNKNOWN);
            }
        }
//        if (CollectionUtils.isEmpty(modelIds)) {
//            return instanceParamMap;
//        }
//        //查询数据库信息
//        List<QueryInstanceParam> queryInstanceParams = mwModelManageDao.selectModelSysClassById(new ArrayList(modelIds));
//        if (CollectionUtils.isEmpty(queryInstanceParams)) {
//            return instanceParamMap;
//        }
//        instanceParamMap = queryInstanceParams.stream().collect(Collectors.toMap(QueryInstanceParam::getModelInstanceId, Function.identity()));
        return modelAssetsDtos;
    }
}
