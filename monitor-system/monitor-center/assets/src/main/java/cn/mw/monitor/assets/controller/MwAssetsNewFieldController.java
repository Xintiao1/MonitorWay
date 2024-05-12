package cn.mw.monitor.assets.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.assets.dto.MwAssetsCustomFieldDto;
import cn.mw.monitor.assets.service.MwAssetsNewFieldService;
import cn.mw.monitor.customPage.api.param.UpdateCustomPageParam;
import cn.mw.monitor.service.assets.api.MwTangibleAssetsService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @ClassName MwAssetsNewFieldController
 * @Description ToDo
 * @Author gengjb
 * @Date 2022/7/5 10:26
 * @Version 1.0
 **/
@RequestMapping("/mwapi/assets/customfield")
@Controller
@Slf4j
@Api(value = "资产自定义监控项字段", tags = "资产自定义监控项字段")
public class MwAssetsNewFieldController  extends BaseApiService {

    @Autowired
    private MwTangibleAssetsService mwTangService;

    @Autowired
    private MwAssetsNewFieldService fieldService;


    /**
     * 查询所有监控项信息
     * @return
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/item/dropDown")
    @ResponseBody
    public ResponseBase selectAllMonitorItem() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwTangService.selectAllMonitorItem();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("MwAssetsNewFieldController{selectAllMonitorItem}",e);
            return setResultFail("selectAllMonitorItem() error", "");
        }
        return setResultSuccess(reply);
    }

    /**
     * 创建资产自定义字段
     * @return
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/create")
    @ResponseBody
    public ResponseBase createAssetsCustomField(@RequestBody MwAssetsCustomFieldDto customFieldDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = fieldService.addAssetsCustomField(customFieldDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("createAssetsCustomField() error", "");
            }
        } catch (Throwable e) {
            log.error("MwAssetsNewFieldController{createAssetsCustomField}",e);
            return setResultFail("createAssetsCustomField() error", "");
        }
        return setResultSuccess(reply);
    }


    /**
     * 修改资产自定义字段
     * @return
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/editor")
    @ResponseBody
    public ResponseBase updateAssetsCustomField(@RequestBody MwAssetsCustomFieldDto customFieldDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = fieldService.updateAssetsCustomField(customFieldDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("updateAssetsCustomField() error", "");
            }
        } catch (Throwable e) {
            log.error("MwAssetsNewFieldController{updateAssetsCustomField}",e);
            return setResultFail("updateAssetsCustomField() error", "");
        }
        return setResultSuccess(reply);
    }


    /**
     * 删除资产自定义字段
     * @return
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/delete")
    @ResponseBody
    public ResponseBase deleteAssetsCustomField(@RequestBody MwAssetsCustomFieldDto customFieldDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = fieldService.deleteAssetsCustomField(customFieldDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("MwAssetsNewFieldController{deleteAssetsCustomField}",e);
            return setResultFail("deleteAssetsCustomField() error", "");
        }
        return setResultSuccess(reply);
    }


    /**
     * 查询资产自定义字段
     * @return
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/browse")
    @ResponseBody
    public ResponseBase selectAssetsCustomField(@RequestBody MwAssetsCustomFieldDto customFieldDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = fieldService.selectAssetsCustomField(customFieldDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("MwAssetsNewFieldController{selectAssetsCustomField}",e);
            return setResultFail("selectAssetsCustomField() error", "");
        }
        return setResultSuccess(reply);
    }

    /**
     * 资产字段排序
     * @return
     */
    @MwPermit(moduleName = "assets_manage")
    @PostMapping("/sort")
    @ResponseBody
    public ResponseBase assetsFieldSort(@RequestBody UpdateCustomPageParam customPageParam) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = fieldService.assetsFieldSort(customPageParam.getModels());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("MwAssetsNewFieldController{assetsFieldSort}",e);
            return setResultFail("assetsFieldSort() error", "");
        }
        return setResultSuccess(reply);
    }
}
