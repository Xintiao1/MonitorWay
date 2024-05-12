package cn.mw.monitor.util.dao;

import cn.mw.monitor.service.user.dto.SettingDTO;

public interface MwUserMapperDao {

    SettingDTO selectSettingsInfo();

}
