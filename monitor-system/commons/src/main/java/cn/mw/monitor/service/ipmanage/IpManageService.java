package cn.mw.monitor.service.ipmanage;

import cn.mw.monitor.service.ipmanage.model.IpManageTree;
import com.github.pagehelper.PageInfo;

import java.util.Date;
import java.util.List;

/**
 * @author lumingming
 * @createTime 20230312 22:09
 * @description
 */
public interface IpManageService {
     PageInfo<IpManageTree> countFenOrHui(Date StartTime, Date EndTime, Integer pageNumber, Integer pageSize);

     List<IpManageTree> getTreeFrist(Date StartTime, Date EndTime, Integer pageNumber, Integer pageSize,Integer id);
}
