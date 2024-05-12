package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.assetsSubType.api.param.AssetsSubType.QueryAssetsSubTypeParam;
import cn.mw.monitor.assetsSubType.api.param.DeleteAssetsSubTypeParam;
import cn.mw.monitor.assetsSubType.dto.TypeTreeDTO;
import cn.mw.monitor.assetsSubType.model.MwAssetsSubTypeTable;
import cn.mw.monitor.assetsSubType.service.MwAssetsSubTypeService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;

/**
 * @author baochengbin
 * @date 2020/3/17
 */

@RequestMapping("/mwapi")
@Controller
@Slf4j
@Api("资产子分类信息")
public class MWAssetsSubTypeController extends BaseApiService {
    private static final Logger logger = LoggerFactory.getLogger("control-" + MWAssetsSubTypeController.class.getName());

    @Autowired
    MwAssetsSubTypeService mwAssetsSubTypeService;

    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assetsSubType/updateGroupId")
    @ResponseBody
    public ResponseBase updateGroupId(HttpServletRequest request, RedirectAttributesModelMap model){
        Reply reply;
        try{
            // 验证内容正确性
            reply = mwAssetsSubTypeService.updateAssetsGroupId();
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            logger.error(e.getMessage());
            return setResultFail("MWAssetsSubTypeController{} updateGroupId() error", "");
        }

        return setResultSuccess(reply);
    }

    /**
     * 新增资产子分类
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assetsSubType/create")
    @ResponseBody
    public ResponseBase addEngineMange(@RequestBody MwAssetsSubTypeTable aParam,
                                      HttpServletRequest request, RedirectAttributesModelMap model){
        Reply reply;
        try{
            // 验证内容正确性
            reply = mwAssetsSubTypeService.insert(aParam);
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            logger.error(e.getMessage());
            return setResultFail("MWAssetsSubTypeController{} addEngineMange() error", "");
        }

        return setResultSuccess(reply);
    }



    /**
     * 删除资产子分类
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assetsSubType/delete")
    @ResponseBody
    public ResponseBase deleteEnigneManage(@RequestBody DeleteAssetsSubTypeParam dParam,
                                         HttpServletRequest request, RedirectAttributesModelMap model){
        Reply reply;
        try{
            // 验证内容正确性
            reply= mwAssetsSubTypeService.delete(dParam.getTypeIdList());
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            logger.error(e.getMessage());
            return setResultFail("MWAssetsSubTypeController{} deleteEnigneManage() error", "");
        }

        return setResultSuccess(reply);
    }

    /**
     * 修改资产子分类
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assetsSubType/editor")
    @ResponseBody
    public ResponseBase updateEngineMange(@RequestBody MwAssetsSubTypeTable auParam,
                                      HttpServletRequest request, RedirectAttributesModelMap model){
        Reply reply;
        try{
            // 验证内容正确性
            reply = mwAssetsSubTypeService.update(auParam);
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            logger.error(e.getMessage());
            return setResultFail("MWAssetsSubTypeController{} updateEngineMange() error", "");
        }

        return setResultSuccess(reply);
    }

    /**
     *  分页查询资产子分类
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assetsSubType/browse")
    @ResponseBody
    public ResponseBase browseEngineMange(@RequestBody QueryAssetsSubTypeParam qParam,
                                           HttpServletRequest request, RedirectAttributesModelMap model){
        Reply reply;
        try{
            // 验证内容正确性
             reply = mwAssetsSubTypeService.selectList(qParam);
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            logger.error(e.getMessage());
            return setResultFail("MWAssetsSubTypeController{} browseEngineMange() error", "");
        }
        return setResultSuccess(reply);
    }

    /**
     * 资产子分类下拉框查询
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assetsSubType/dropdown/browse")
    @ResponseBody
    public ResponseBase typeDropdownBrowse(@RequestBody  QueryAssetsSubTypeParam qsParam,
            HttpServletRequest request, RedirectAttributesModelMap model){
        Reply reply;
        try{
            // 验证内容正确性
            reply = mwAssetsSubTypeService.selectDorpdownList(qsParam);
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            logger.error(e.getMessage());
            return setResultFail("MWAssetsSubTypeController{} typeDropdownBrowse() error", "");
        }

        return setResultSuccess(reply);
    }

    /**
     * 查询所有资产分类树形展示所有资产类型
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/assetsSubType/treeList/browse")
    @ResponseBody
    public ResponseBase getTreeList(@RequestBody TypeTreeDTO typeTreeDTO,
                                           HttpServletRequest request, RedirectAttributesModelMap model){
        Reply reply;
        try{
            // 验证内容正确性
            reply = mwAssetsSubTypeService.selectTypeTrees(typeTreeDTO);
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            logger.error(e.getMessage());
            return setResultFail("MWAssetsSubTypeController{} getTreeList() error", "");
        }

        return setResultSuccess(reply);
    }

    /**
     * 资产类型下拉框查询；资产子类型下拉框查询
     */
    @MwPermit(moduleName = "assets_manage")
    @GetMapping("/assetsType/dropdown/browse")
    @ResponseBody
    public ResponseBase typeDropdownBrowse(@PathParam("subTypeFlag") boolean subTypeFlag, @PathParam("classify")  Integer classify,
                                           HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try{
            // 验证内容正确性
            reply = mwAssetsSubTypeService.selectDorpdownList(subTypeFlag,classify);
            if(null != reply && reply.getRes() != PaasConstant.RES_SUCCESS){
                return setResultFail(reply.getMsg(), reply.getData());
            }
        }catch (Throwable e){
            logger.error(e.getMessage());
            return setResultFail("MWAssetsSubTypeController{} typeDropdownBrowse() error", "");
        }

        return setResultSuccess(reply);
    }
}
