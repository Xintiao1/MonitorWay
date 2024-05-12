package cn.mw.time;

/*import cn.mw.monitor.assets.dao.MwTangibleAssetsTableDao;*/
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;
import cn.mw.monitor.ipaddressmanage.dao.MwIpAddressManageListTableDao;
import cn.mw.monitor.ipaddressmanage.param.QueryIpAddressManageListParam;
import cn.mw.monitor.ipaddressmanage.param.TypeDESC;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author bkc
 * @date 2020/7/24
 */
@Component
//@ConditionalOnProperty(prefix = "scheduling", name = "enabled", havingValue = "true")
@EnableScheduling
@Slf4j
public class MWIpAddressTime {

    @Resource
    MwIpAddressManageListTableDao mwIpAddressManageListTableDao;

/*    @Resource
    MwTangibleAssetsTableDao mwTangibleAssetsTableDao;*/


//    @Scheduled(cron = "0 0  */4 * * ?") //4小时执行一次
//    @Async
    //初始化ip地址清单
 /*   public void initIpAddressManage(){
        //查询出需要4小时更新一次数据的 数据
        List<QueryIpAddressManageListParam> list = mwIpAddressManageListTableDao.selectListByInterval(4);

        //先更新ip_state
        for(QueryIpAddressManageListParam entity:list){
            String ip = entity.getIpAddress();
            List<MwTangibleassetsDTO> result = mwTangibleAssetsTableDao.checkIpAddress(ip);
            if(result.size()>0){
                entity.setIpState(Integer.parseInt(TypeDESC.USERED.getName()));
            }else{
                entity.setIpState(Integer.parseInt(TypeDESC.NOTUSER.getName()));
            }
        }

        //保存数据
        //mwIpAddressManageListTableDao.updateBatch(list);

    }
*/


}
