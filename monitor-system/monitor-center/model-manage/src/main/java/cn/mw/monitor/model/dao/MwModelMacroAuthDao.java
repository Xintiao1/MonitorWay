package cn.mw.monitor.model.dao;

import cn.mw.monitor.model.dto.ModelMacroSelectDataDTO;
import cn.mw.monitor.model.dto.MwModelMacrosManageDTO;
import cn.mw.monitor.model.param.DeleteModelMacrosParam;
import cn.mw.monitor.model.param.MwModelMacrosManageParam;
import cn.mw.monitor.model.param.MwModelMacrosValInfoParam;

import java.util.List;

/**
 * @author qzg
 * @date 2023/6/12
 */
public interface MwModelMacroAuthDao {
    List<MwModelMacrosManageDTO> getMacroAuthList(MwModelMacrosManageParam param);

    List<ModelMacroSelectDataDTO> getAllMacroField();

    List<MwModelMacrosManageParam> queryMacroFieldByModelId(Integer modelId);

    int checkAuthNameInfoNum(MwModelMacrosManageParam param);

    void insertModelMacroInfoMapper(List<MwModelMacrosManageParam> list);

    void deleteModelMacroInfoMapper(List<MwModelMacrosManageParam> list);

    void deleteModelMacroNameInfo(List<MwModelMacrosManageParam> list);

    void addMacroValueAuthName(List<MwModelMacrosManageParam> list);

   void editorMacroValueAuthNameInfo(List<MwModelMacrosManageParam> list);

   List<MwModelMacrosManageDTO> selectInfoPopup(MwModelMacrosManageParam param);

   void deleteModelMacroNameInfoByName(List<DeleteModelMacrosParam> param);

}
