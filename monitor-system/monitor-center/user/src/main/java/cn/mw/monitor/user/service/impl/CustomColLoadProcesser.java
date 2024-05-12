package cn.mw.monitor.user.service.impl;

import cn.mw.monitor.service.model.param.MwPagefieldByModelTable;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.customPage.dao.MwCustomcolTableDao;
import cn.mw.monitor.customPage.dao.MwPagefieldTableDao;
import cn.mw.monitor.customPage.model.MwCustomcolTable;
import cn.mw.monitor.customPage.model.MwPagefieldTable;
import cn.mw.monitor.event.Event;
import cn.mw.monitor.service.common.ServiceException;
import cn.mw.monitor.user.service.CustomColLoad;
import cn.mw.monitor.user.service.IMWUserPostProcesser;
import cn.mw.monitor.event.CustomColLoadEvent;
import cn.mwpaas.common.utils.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author baochengbin
 * @date 2020/3/26
 */
@Service
@Slf4j
public class CustomColLoadProcesser implements CustomColLoad, IMWUserPostProcesser {

    private static final Logger logger = LoggerFactory.getLogger("service-" + CustomColLoadProcesser.class.getName());

    @Resource
    MwCustomcolTableDao mwCustomcolTableDao;

    @Resource
    MwPagefieldTableDao mwPagefieldTableDao;

    private final static int INSERT_MAX_SIZE = 50;

    @Override
    public List<Reply> handleEvent(Event event) throws Throwable {
        //新建用户时初始化个性化设置
        if (event instanceof CustomColLoadEvent) {
            CustomColLoadEvent customEvent = (CustomColLoadEvent) event;
            List<Reply> faillist = processCustomColLoad(customEvent.getUserIds());
            List<Reply> faillist1 = processCustomColByModelLoad(customEvent.getUserIds());
            faillist.addAll(faillist1);
            return faillist;
        }
        return null;
    }

    @Override
    public List<Reply> processCustomColLoad(List<Integer> userIds) {
        List<Reply> faillist = new ArrayList<Reply>();
        List<MwCustomcolTable> customTables = new ArrayList<>();
        try {
            mwPagefieldTableDao.deleteByUserId(userIds);
            List<MwPagefieldTable> pagefieldTables = mwPagefieldTableDao.seletctAll();

            for (Integer userId : userIds) {
                for(int i = 0;i<pagefieldTables.size(); i++){
                    MwCustomcolTable customTable = new MwCustomcolTable();
                    customTable.setUserId(userId);
                    customTable.setColId(pagefieldTables.get(i).getId());
                    customTable.setSortable(true);
                    customTable.setWidth(null);
                    customTable.setVisible(pagefieldTables.get(i).getImportance());
                    customTable.setOrderNumber(pagefieldTables.get(i).getOrderNum());
                    customTables.add(customTable);
                    if (customTables.size() >= INSERT_MAX_SIZE){
                        mwCustomcolTableDao.insert(customTables);
                        customTables.clear();
                    }
                }
                if (CollectionUtils.isNotEmpty(customTables)){
                    mwCustomcolTableDao.insert(customTables);
                    customTables.clear();
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            faillist.add(Reply.fail(e.getMessage()));
            throw new ServiceException(faillist);
        }
        return faillist;
    }

    /**
     * 对用户初始化模型管理的列字段
     * @param userIds
     * @return
     */
    @Override
    public List<Reply> processCustomColByModelLoad(List<Integer> userIds) {
        List<Reply> faillist = new ArrayList<Reply>();
        List<MwCustomcolTable> customTables = new ArrayList<>();
        try {
            mwPagefieldTableDao.deleteByModelUserId(userIds);
            List<MwPagefieldByModelTable> pagefieldByModelTables = mwPagefieldTableDao.seletctAllByModel();

            for (Integer userId : userIds) {
                for(int i = 0;i<pagefieldByModelTables.size(); i++){
                    MwCustomcolTable customTable = new MwCustomcolTable();
                    customTable.setUserId(userId);
                    customTable.setColId(pagefieldByModelTables.get(i).getId());
                    customTable.setSortable(true);
                    customTable.setWidth(null);
                    customTable.setVisible(pagefieldByModelTables.get(i).getImportance());
                    customTable.setOrderNumber(pagefieldByModelTables.get(i).getOrderNum());
                    customTable.setModelPropertiesId(pagefieldByModelTables.get(i).getModelPropertiesId() == null ? 0 :pagefieldByModelTables.get(i).getModelPropertiesId());
                    customTables.add(customTable);
                    if (customTables.size() >= INSERT_MAX_SIZE){
                        mwCustomcolTableDao.insertByModel(customTables);
                        customTables.clear();
                    }
                }
                if(customTables.size()>0){
                    mwCustomcolTableDao.insertByModel(customTables);
                    customTables.clear();
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            faillist.add(Reply.fail(e.getMessage()));
            throw new ServiceException(faillist);
        }
        return faillist;
    }

}
