package cn.mw.monitor.assets.dao;

import cn.mw.monitor.assets.api.param.assets.AddUpdateAssetsIotParam;
import cn.mw.monitor.assets.api.param.assets.IotTypeParam;
import cn.mw.monitor.assets.dto.AssetsIotDto;
import cn.mw.monitor.assets.dto.SoundParam;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author xhy
 * @date 2020/6/6 10:48
 */
public interface MwAssetsIotDao {
    int selectAssetsIotId(String assetsId);

    AssetsIotDto selectAssetsIot(String assetsId);

    int addAssetsIot(AddUpdateAssetsIotParam addUpdateAssetsIotParam);

    int updateAssetsIot(AddUpdateAssetsIotParam addUpdateAssetsIotParam);

    int deleteAssetsIot(String assetsId);

    int updateVoice(SoundParam soundParam);

    int  insertVoice(SoundParam soundParam);

    List<IotTypeParam> selectIotTypeList(@Param("assetsIds") List<String> assetsIds);
}
