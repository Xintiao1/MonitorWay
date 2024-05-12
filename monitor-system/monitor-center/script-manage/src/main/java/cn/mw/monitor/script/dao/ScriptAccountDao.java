package cn.mw.monitor.script.dao;

import cn.mw.monitor.script.entity.MwHomeworkAlert;
import cn.mw.monitor.script.entity.MwHomeworkAlertMapper;
import cn.mw.monitor.script.entity.ScriptAccountEntity;
import cn.mw.monitor.script.entity.ScriptOutAsssets;
import cn.mw.monitor.script.param.ScriptAccountParam;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author gui.quanwang
 * @className ScriptAccountDao
 * @description 账户管理Dao
 * @date 2022/4/24
 */
public interface ScriptAccountDao extends BaseMapper<ScriptAccountEntity> {

    /**
     * 获取整合后的账户列表数据
     * @param param
     * @return
     */
    List<ScriptAccountParam> getAccountList(ScriptAccountParam param);

    /**
     * 查询账户管理模糊数据
     *
     * @return
     */
    List<Map<String, String>> fuzzSearchAccountData();

    Integer selectPwdAnd(@Param("s") String s,@Param("encrypt")  String encrypt,@Param("port") String port);

    void insertScriptOut(@Param("scriptOutAsssets")  ScriptOutAsssets scriptOutAsssets);

    void removAssets(@Param("ids")  List<Integer> ids);

    Integer selectCountAssets(@Param("accountId")Integer accountId, @Param("ipAddress")String ipAddress);

    void updateAssets(@Param("scriptOutAsssets") ScriptOutAsssets scriptOutAsssets);

    List<MwHomeworkAlert> getAllAlertBrowse(@Param("mwHomeworkAlert") MwHomeworkAlert mwHomeworkAlert);

    void alertCreate(@Param("mwHomeworkAlert") MwHomeworkAlert mwHomeworkAlert);

    void alertEditor(@Param("mwHomeworkAlert") MwHomeworkAlert mwHomeworkAlert);

    void removeAlert(@Param("ids") List<Integer> id);

    void addAlertNum(@Param("id") Integer id);

    void addMwHomeWorkAlertMapper(@Param("mwHomeworkAlertMappers")  List<MwHomeworkAlertMapper> mwHomeworkAlertMappers);

    List<MwHomeworkAlertMapper> getListMwHomeWorkMapper(@Param("id") Integer id);

    void deteAssetsId(@Param("id") Integer id);
}
