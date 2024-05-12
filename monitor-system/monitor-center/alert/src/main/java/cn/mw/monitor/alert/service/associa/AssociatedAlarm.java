package cn.mw.monitor.alert.service.associa;

import cn.mw.monitor.common.web.ApplicationContextProvider;
import cn.mw.monitor.alert.dao.MWAlertAssetsDao;
import cn.mw.monitor.service.assets.model.MwTangibleassetsDTO;

/**
 * @author xhy
 * @date 2021/1/29 16:53
 * 关联告警
 */
public abstract class AssociatedAlarm {

    protected MWAlertAssetsDao assetsDao;

    protected MwTangibleassetsDTO mwTangibleassetsDTO;

    protected Boolean isActive;

    protected abstract String getAssociatedAlarm();


    public AssociatedAlarm(MwTangibleassetsDTO mwTangibleassetsDTO, Boolean isActive) {
        MWAlertAssetsDao assetsDao = ApplicationContextProvider.getBean(MWAlertAssetsDao.class);
        this.assetsDao = assetsDao;
        this.mwTangibleassetsDTO = mwTangibleassetsDTO;
        this.isActive = isActive;
    }


}
