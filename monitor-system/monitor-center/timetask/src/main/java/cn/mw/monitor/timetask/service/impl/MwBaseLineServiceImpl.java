package cn.mw.monitor.timetask.service.impl;

import cn.mw.monitor.service.timetask.api.MwBaseLineValueService;
import cn.mw.monitor.service.timetask.dto.MwBaseLineHealthValueCommonsDto;
import cn.mw.monitor.service.timetask.dto.MwBaseLineHealthValueCommonsParam;
import cn.mw.monitor.service.timetask.dto.MwBaseLineHealthValueResultParam;
import cn.mw.monitor.service.timetask.dto.SelectNameEnum;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mw.monitor.timetask.dao.MwBaseLineDao;
import cn.mw.monitor.timetask.entity.MwBaseLineHealthValueDto;
import cn.mw.monitor.timetask.entity.MwBaseLineItemNameDto;
import cn.mw.monitor.timetask.entity.MwBaseLineManageDto;
import cn.mw.monitor.timetask.service.MwBaseLineService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mwpaas.common.utils.CollectionUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName MwBaseLineServiceImpl
 * @Description 基线模块实现
 * @Author gengjb
 * @Date 2022/4/6 10:32
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwBaseLineServiceImpl implements MwBaseLineService, MwBaseLineValueService {

    @Resource
    private MwBaseLineDao baseLineDao;

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    /**
     * 查询基线模块的监控项
     * @return
     */
    @Override
    public Reply getItemName(MwBaseLineItemNameDto baseLineItemNameDto) {
        try {
            Integer pageSize = baseLineItemNameDto.getPageSize();
            Integer pageNumber = baseLineItemNameDto.getPageNumber();
            List<MwBaseLineItemNameDto> list = new ArrayList<>();
            list = baseLineDao.getItemNames();
            //查询所有已设置的item，进行数据去重
            List<String> itemIds = baseLineDao.getItemIds();
            if(CollectionUtils.isNotEmpty(itemIds) && CollectionUtils.isNotEmpty(list)){
                List<Integer> ids = new ArrayList<>();
                for (String itemId : itemIds) {
                    if(StringUtils.isBlank(itemId))continue;
                    int[] items = Arrays.stream(itemId.split(",")).mapToInt(s -> Integer.parseInt(s)).toArray();
                    List<Integer> as = Arrays.stream(items).boxed().collect(Collectors.toList());
                    ids.addAll(as);
                }
                Iterator<MwBaseLineItemNameDto> iterator = list.iterator();
                while (iterator.hasNext()){
                    MwBaseLineItemNameDto next = iterator.next();
                    if(ids.contains(next.getId())){
                        iterator.remove();
                    }
                }
            }
            int fromIndex = pageSize * (pageNumber -1);
            int toIndex = pageSize * pageNumber;
            if(toIndex > list.size()){
                toIndex = list.size();
            }
            List<MwBaseLineItemNameDto> realData = list.subList(fromIndex, toIndex);
            PageInfo pageInfo = new PageInfo<>(realData);
            pageInfo.setTotal(list.size());
            pageInfo.setList(realData);
            return Reply.ok(pageInfo);
        }catch (Exception e){
            log.error("查询基线监控项失败,失败信息:",e);
            return Reply.fail("查询基线监控项失败");
        }
    }

    /**
     * 基线模块数据增加
     * @param baseLineManageDto 基线添加的数据
     * @return
     */
    @Override
    public Reply addBaseLineData(MwBaseLineManageDto baseLineManageDto) {
        try {
            //将itemIds转为字符串形式存储
            List<Integer> itemIds = baseLineManageDto.getItemIds();
            String itemIdStr = StringUtils.join(itemIds.toArray(),",");
            baseLineManageDto.setItemIdStr(itemIdStr);
            baseLineManageDto.setCreator(iLoginCacheInfo.getLoginName());
            baseLineManageDto.setCreateDate(new Date());
            //数据添加
            int count = baseLineDao.insertBaseLine(baseLineManageDto);
            if(count > 0){
                return Reply.ok("添加成功");
            }else{
                return Reply.fail("添加基线失败");
            }
        }catch (Exception e){
            log.error("添加基线失败,失败信息:",e);
            return Reply.fail("添加基线失败");
        }
    }

    /**
     * 基线模块数据修改
     * @param baseLineManageDto 基线修改的数据
     * @return
     */
    @Override
    public Reply updateBaseLineData(MwBaseLineManageDto baseLineManageDto) {
        try {
            if(StringUtils.isBlank(baseLineManageDto.getName()))return Reply.fail("基线名称不能为空");
            //将itemIds转为字符串形式存储
            List<Integer> itemIds = baseLineManageDto.getItemIds();
            String itemIdStr = "";
            if(CollectionUtils.isNotEmpty(itemIds)){
                itemIdStr = StringUtils.join(itemIds.toArray(),",");
            }
            baseLineManageDto.setItemIdStr(itemIdStr);
            baseLineManageDto.setModifier(iLoginCacheInfo.getLoginName());
            //数据添加
            baseLineDao.updateBaseLine(baseLineManageDto);
            return Reply.ok("修改成功");
        }catch (Exception e){
            log.error("修改基线失败,失败信息:",e);
            return Reply.fail("修改基线失败");
        }
    }

    /**
     * 删除基线数据
     * @param baseLineManageDto 基线删除的id
     * @return
     */
    @Override
    public Reply deleteBaseLineData(MwBaseLineManageDto baseLineManageDto) {
        try {
            List<Integer> ids = baseLineManageDto.getIds();
            if(CollectionUtils.isEmpty(ids))return Reply.ok("删除成功");
            //数据删除
            int count = baseLineDao.deleteBaseLine(ids);
            if(count > 0){
                return Reply.ok("删除成功");
            }else{
                return Reply.fail("删除基线失败");
            }
        }catch (Exception e){
            log.error("删除基线失败,失败信息:",e);
            return Reply.fail("删除基线失败");
        }
    }

    /**
     * 查询基线数据
     * @param baseLineManageDto 查询参数
     * @return
     */
    @Override
    public Reply selectBaseLineData(MwBaseLineManageDto baseLineManageDto) {
        try {
            PageHelper.startPage(baseLineManageDto.getPageNumber(), baseLineManageDto.getPageSize());
            List<MwBaseLineManageDto> mwBaseLineManageDtos = new ArrayList<>();
            mwBaseLineManageDtos = baseLineDao.selectBaseLine(baseLineManageDto);
            PageInfo pageInfonull = new PageInfo(mwBaseLineManageDtos);
            pageInfonull.setList(mwBaseLineManageDtos);
            if(CollectionUtils.isEmpty(mwBaseLineManageDtos)) return Reply.ok(pageInfonull);
            for (MwBaseLineManageDto mwBaseLineManageDto : mwBaseLineManageDtos) {
                String itemIdStr = mwBaseLineManageDto.getItemIdStr();
                if(StringUtils.isBlank(itemIdStr))continue;
                int[] ids = Arrays.stream(itemIdStr.split(",")).mapToInt(s -> Integer.parseInt(s)).toArray();
                List<Integer> itemIds = Arrays.stream(ids).boxed().collect(Collectors.toList());
                //查询监控项数据
                List<MwBaseLineItemNameDto> itemNamesByIds = baseLineDao.getItemNamesByIds(itemIds);
                mwBaseLineManageDto.setItemNameDtos(itemNamesByIds);
                mwBaseLineManageDto.setItemIds(itemIds);
            }
            PageInfo pageInfo = new PageInfo(mwBaseLineManageDtos);
            pageInfo.setList(mwBaseLineManageDtos);
            return Reply.ok(pageInfo);
        }catch (Exception e){
            log.error("查询基线失败,失败信息:",e);
            return Reply.fail("查询基线失败");
        }
    }

    /**
     * 查询基线健康值
     * @param itemNames 监控项名称集合
     * @param assetsId 资产主机ID
     * @return
     */
    @Override
    public Reply selectBaseLineHealthValue(List<String> itemNames, String assetsId) {
        List<MwBaseLineHealthValueDto> healthValue = baseLineDao.getHealthValue(itemNames, assetsId);
        List<MwBaseLineHealthValueCommonsDto> commonsDtos = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(healthValue)){
            for (MwBaseLineHealthValueDto baseLineHealthValueDto : healthValue) {
                MwBaseLineHealthValueCommonsDto commonsDto = MwBaseLineHealthValueCommonsDto.builder().assetsId(baseLineHealthValueDto.getAssetsId())
                        .id(baseLineHealthValueDto.getId())
                        .itemName(baseLineHealthValueDto.getItemName()).value(baseLineHealthValueDto.getValue()).assetsName(baseLineHealthValueDto.getAssetsName()).build();
                commonsDtos.add(commonsDto);
            }
        }
        return Reply.ok(commonsDtos);
    }

    @Override
    public Reply selectHealthValueByAssets() {
        List<MwBaseLineHealthValueResultParam> resultParams = new ArrayList<>();
        for(SelectNameEnum name : SelectNameEnum.values()){
            MwBaseLineHealthValueResultParam temp = new MwBaseLineHealthValueResultParam();
            temp.setAssetsName(name.getName());
            if(name.getName().equals(SelectNameEnum.TARGET.getName())){
                List<MwBaseLineItemNameDto> list = new ArrayList<>();
                list = baseLineDao.getItemNames();
                Map<String,String> itemNameMap = new HashMap<>();
                for (MwBaseLineItemNameDto dto : list) {
                    itemNameMap.put(dto.getItemName(),dto.getName());
                }
                List<MwBaseLineHealthValueCommonsParam> result = new ArrayList<>();
                Reply reply = selectBaseLineHealthValue(null,null);
                List<MwBaseLineHealthValueCommonsDto> commonsDtos = new ArrayList<>();
                if (null != reply && reply.getRes() == PaasConstant.RES_SUCCESS) {
                    commonsDtos = (List<MwBaseLineHealthValueCommonsDto>) reply.getData();
                    if (CollectionUtils.isNotEmpty(commonsDtos)) {
                        Map<String,List<MwBaseLineHealthValueCommonsDto>> collect = commonsDtos.stream().collect(Collectors.groupingBy(MwBaseLineHealthValueCommonsDto::getAssetsId));
                        for (String key : collect.keySet()){
                            List<MwBaseLineHealthValueCommonsDto> tempDto = collect.get(key);
                            MwBaseLineHealthValueCommonsParam lhv = new MwBaseLineHealthValueCommonsParam();
                            lhv.setAssetsId(key);
                            lhv.setAssetsName(tempDto.get(0).getAssetsName() + "-" + key);
                            lhv.setAssetsNames(tempDto);
                            for (MwBaseLineHealthValueCommonsDto dto : tempDto){
                                String assetsName = dto.getItemName();
                                if(itemNameMap.get(dto.getItemName()) != null){
                                    assetsName = itemNameMap.get(dto.getItemName());
                                }
                                dto.setAssetsName(assetsName);
                            }
                            result.add(lhv);
                        }
                    }
                    temp.setAssetsNames(result);
                }
            }
            resultParams.add(temp);
        }
        return Reply.ok(resultParams);
    }

    @Override
    public List<Map<String, Object>> getBaseLineAllData() {
        return baseLineDao.getAllHealthValue();
    }
}
