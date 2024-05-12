package cn.mw.monitor.visualized.service.impl.detailimpl;

import cn.mw.monitor.service.alert.api.MWAlertService;
import cn.mw.monitor.service.zbx.param.AlertParam;
import cn.mw.monitor.visualized.service.MwVisualizedZkSoftWare;
import cn.mwpaas.common.model.Reply;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName
 * @Description MwVisualizedZkSoftWareAlert 中控告警数据返回
 * @Author gengjb
 * @Date 2023/3/15 20:17
 * @Version 1.0
 **/
@Service
@Slf4j
public class MwVisualizedZkSoftWareAlert implements MwVisualizedZkSoftWare {

    @Autowired
    private MWAlertService mwalertService;

    @Override
    public int[] getType() {
        return new int[]{41};
    }

    @Override
    public Object getData() {
        try {
            AlertParam alertParam = new AlertParam();
            alertParam.setPageSize(Integer.MAX_VALUE);
            Reply reply = mwalertService.getCurrAlertPage(alertParam);
            PageInfo pageInfo = (PageInfo) reply.getData();
            return pageInfo.getList();
        }catch (Throwable e){
            log.error("可视化查询中控告警数据失败",e);
        }
        return null;
    }
}
