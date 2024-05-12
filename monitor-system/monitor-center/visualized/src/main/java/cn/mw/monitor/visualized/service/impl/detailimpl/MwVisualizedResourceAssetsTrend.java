package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.util.MwVisualizedDateUtil;
import cn.mw.monitor.visualized.dao.MwVisualizedManageDao;
import cn.mw.monitor.visualized.dto.MwVisualizedAeestsCountDto;
import cn.mw.monitor.visualized.param.MwVisualizedModuleParam;
import cn.mw.monitor.visualized.service.MwVisualizedManageService;
import cn.mw.monitor.visualized.service.MwVisualizedModule;
import cn.mwpaas.common.utils.CollectionUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @ClassName
 * @Description 资产类型分类统计
 * @Author gengjb
 * @Date 2023/5/17 14:26
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedResourceAssetsTrend implements MwVisualizedModule {

    @Autowired
    private MwVisualizedManageService visualizedManageService;

    @Resource
    private MwVisualizedManageDao visualizedManageDao;

    @Override
    public int[] getType() {
        return new int[]{66};
    }

    @Override
    public Object getData(Object data) {
        try {
            if(data == null){return null;}
            MwVisualizedModuleParam moduleParam = (MwVisualizedModuleParam) data;
            //获取分区的实例
            List<MwTangibleassetsDTO> tangibleassetsDTOS = visualizedManageService.getModelAssets(moduleParam,false);
            //获取最近7天的时间
            List<String> days = MwVisualizedDateUtil.getDays(7);
            //根据时间和名称查询数据
            List<MwVisualizedAeestsCountDto> aeestsCountDtos = visualizedManageDao.selectVisualizedPartitionAssets(moduleParam.getServerName(), days);
            if(aeestsCountDtos == null){
                aeestsCountDtos = new ArrayList<>();
            }
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            MwVisualizedAeestsCountDto countDto = new MwVisualizedAeestsCountDto();
            countDto.extractFrom(moduleParam.getServerName(),tangibleassetsDTOS.size(),format.format(new Date()));
            aeestsCountDtos.add(countDto);
            //按时间排序
            if(CollectionUtils.isEmpty(aeestsCountDtos)){return null;}
            dateSort(aeestsCountDtos,format);
            //主机状态统计
            return aeestsCountDtos;
        }catch (Throwable e){
            log.error("MwVisualizedResourceAssetsTrend{} getData::",e);
            return null;
        }
    }

    /**
     * 告警按时间排序
     */
    private void dateSort( List<MwVisualizedAeestsCountDto> aeestsCountDtos,SimpleDateFormat format){
        Collections.sort(aeestsCountDtos, new Comparator<MwVisualizedAeestsCountDto>() {
            @SneakyThrows
            @Override
            public int compare(MwVisualizedAeestsCountDto o1, MwVisualizedAeestsCountDto o2) {
                if(format.parse(o1.getTime()).compareTo(format.parse(o2.getTime())) > 0){
                    return 1;
                }
                if(format.parse(o1.getTime()).compareTo(format.parse(o2.getTime())) < 0){
                    return -1;
                }
                return 0;
            }
        });
    }
}
