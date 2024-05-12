package cn.mw.monitor.script.dao;

import cn.mw.monitor.script.entity.ScriptManageEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;
import java.util.Map;

/**
 * @author gui.quanwang
 * @className ScriptManageDao
 * @description 脚本管理Dao层
 * @date 2022/4/8
 */
public interface ScriptManageDao extends BaseMapper<ScriptManageEntity> {

    /**
     * 查询脚本管理模糊数据
     *
     * @return
     */
    List<Map<String, String>> fuzzSearchScriptData();

}
