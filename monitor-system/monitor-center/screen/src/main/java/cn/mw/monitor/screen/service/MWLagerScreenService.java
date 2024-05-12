package cn.mw.monitor.screen.service;

import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.screen.dto.PermDto;
import cn.mw.monitor.screen.model.FilterAssetsParam;
import cn.mw.monitor.screen.param.*;

import java.util.List;

/**
 * @author xhy
 * @date 2020/4/9 10:41
 */
public interface MWLagerScreenService {

    Reply addLagerScreen(MwLagerScreenParam mwLagerScreenParam);

    Reply updateLagerScreen(MwLagerScreenParam mwLagerScreenParam);

    Reply deleteLagerScreen(String screenId);

    Reply updateEnable(EnableParam enableParam);

    Reply getLayoutBase();

    Reply getModelList();

    Reply getModelType();

    Reply addLagerScreenData(ModelDataParam modelDataParam);

    Reply saveScreenImg(ImgParam imgParam);

    Reply updateModelData(UpdateModelDataParam updateModelDataParam);

    Reply deleteModelData(String modelDataId);

    Reply getLagerScreenList(PermDto permDto);

    Reply editLinkRank(String modelDataId, List<String> linkList,int linkType);

    Reply getLinkOption(String modelDataId);

    Reply getDataListByModelDataId1(String modelDataId,Integer userId);

    // Reply getDataListByModelDataId(String modelDataId);

    Reply getRolePermission();

    Reply updateScreenName(ScreenNameParam screenNameParam);

    Reply editorFilterAssets(FilterAssetsParam param);

    Reply getLagerScreenById(String screenId);

    Reply getFilterAssets(FilterAssetsParam param);

    Reply editIndexLayout(EditorIndexParam param);

    Reply getCoordinate();

    Reply getIcmpLink(boolean first);

}
