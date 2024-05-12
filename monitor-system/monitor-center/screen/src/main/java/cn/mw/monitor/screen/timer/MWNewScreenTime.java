package cn.mw.monitor.screen.timer;

import cn.mw.monitor.bean.TimeTaskRresult;
import cn.mw.monitor.manager.MwAssetsManager;
import cn.mw.monitor.screen.dao.MWNewScreenManageDao;
import cn.mw.monitor.service.assets.model.MwCommonAssetsDto;
import cn.mw.monitor.service.assets.model.MwTangibleassetsTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MWNewScreenTime
 * @Description ToDo
 * @Author gengjb
 * @Date 2021/12/31 11:16
 * @Version 1.0
 **/
@Component
@Slf4j(topic = "timerController")
public class MWNewScreenTime {

    @Autowired
    private MWNewScreenManageDao screenManageDao;

    @Autowired
    private MwAssetsManager mwAssetsManager;

    private static final String ASSETSLIST = "assetsList";

    public static final int ADMIN = 106;

//    @Scheduled(cron = "0 0/5 * * * ?")
    public void censusAssetsAmount(){
        try {
            //查询资产总数量
            //根据类型查询资产数据
            MwCommonAssetsDto mwCommonAssetsDto = new MwCommonAssetsDto();
            mwCommonAssetsDto.setUserId(ADMIN);
            Map<String, Object> assetList = mwAssetsManager.getAssetsByUserId(mwCommonAssetsDto);
            List<MwTangibleassetsTable> mwTangibleassetsTables = new ArrayList<>();
            if(assetList != null){
                mwTangibleassetsTables = (List<MwTangibleassetsTable>) assetList.get(ASSETSLIST);
            }
            int count = mwTangibleassetsTables.size();
            //将资产数量及日期存入数据库
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String censusDate = format.format(new Date());
            //判断是否已有该日期数据
            Integer dataCount = screenManageDao.selectOneDayAssetsCount(censusDate);
            if(dataCount != null && dataCount > 0)return;
            screenManageDao.saveAssetsAmountCensusData(count, censusDate);
            log.info("资产数量统计数据添加成功，统计日期"+censusDate);
            TimeTaskRresult taskRresult = new TimeTaskRresult();
            taskRresult.setSuccess(true);
            taskRresult.setResultType(0);
            taskRresult.setResultContext("每日统计资产数量:成功");
        }catch (Exception e){
            log.error("资产数量统计数据添加失败，统计日期"+new Date(),e);
        }

    }
}
