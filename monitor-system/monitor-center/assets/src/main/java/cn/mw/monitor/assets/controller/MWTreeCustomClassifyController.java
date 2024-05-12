package cn.mw.monitor.assets.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.assets.param.MWTreeCustomClassifyParam;
import cn.mw.monitor.assets.service.MWTreeCustomClassifyService;
import cn.mw.monitor.common.bean.SystemLogDTO;
import cn.mw.monitor.service.user.listener.ILoginCacheInfo;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @ClassName MWTreeCustomClassifyController
 * @Description 树状结构自定义
 * @Author gengjb
 * @Date 2021/9/9 11:34
 * @Version 1.0
 **/
@RequestMapping("/mwapi/assets")
@Controller
@Slf4j
@Api(value = "资产树状结构", tags = "资产树状结构自定义")
public class MWTreeCustomClassifyController extends BaseApiService {

    private static final Logger logger = LoggerFactory.getLogger("MWDBLogger");

    @Autowired
    ILoginCacheInfo iLoginCacheInfo;

    @Autowired
    private MWTreeCustomClassifyService treeCustomClassifyService;

    @PostMapping("/customClassify/create")
    @ResponseBody
    @ApiOperation("树状结构自定义分类新增")
    public ResponseBase createCustomClassify(@RequestBody MWTreeCustomClassifyParam param) {
        Reply reply;
        try {
            SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName("自定义分类新增")
                    .objName(param.getCustomName()).operateDes("新增树状结构自定义分类").build();
            logger.info(JSON.toJSONString(builder));
            reply = treeCustomClassifyService.createCustomClassify(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("树状结构自定义分类新增失败", e);
            return setResultFail("树状结构自定义分类新增失败", "");
        }
    }

    @PostMapping("/customClassify/editor")
    @ResponseBody
    @ApiOperation("树状结构自定义分类修改")
    public ResponseBase updateCustomClassify(@RequestBody MWTreeCustomClassifyParam param) {
        Reply reply;
        try {
            SystemLogDTO builder = SystemLogDTO.builder().userName(iLoginCacheInfo.getLoginName()).modelName("自定义分类修改")
                    .objName(param.getCustomName()).operateDes("修改树状结构自定义分类").build();
            logger.info(JSON.toJSONString(builder));
            reply = treeCustomClassifyService.updateCustomClassify(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("树状结构自定义分类修改失败", e);
            return setResultFail("树状结构自定义分类修改失败", "");
        }
    }

    @PostMapping("/customClassify/delete")
    @ResponseBody
    @ApiOperation("树状结构自定义分类删除")
    public ResponseBase deleteCustomClassify(@RequestBody MWTreeCustomClassifyParam param) {
        Reply reply;
        try {
            reply = treeCustomClassifyService.deleteCustomClassify(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("树状结构自定义分类删除失败", e);
            return setResultFail("树状结构自定义分类删除失败", "");
        }
    }

    @PostMapping("/customClassify/browse")
    @ResponseBody
    @ApiOperation("树状结构自定义分类查询")
    public ResponseBase selectCustomClassify(@RequestBody MWTreeCustomClassifyParam param) {
        Reply reply;
        try {
            reply = treeCustomClassifyService.selectCustomClassify(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("树状结构自定义分类查询失败", e);
            return setResultFail("树状结构自定义分类查询失败", "");
        }
    }

    @PostMapping("/customClassify/getAssetsData")
    @ResponseBody
    @ApiOperation("树状结构自定义分类查询资产数据")
    public ResponseBase selectCustomClassifyAssets(@RequestBody MWTreeCustomClassifyParam param) {
        Reply reply;
        try {
            reply = treeCustomClassifyService.selectCustomClassifyAssets(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("查询资产数据失败", e);
            return setResultFail("查询资产数据失败", "");
        }
    }

}
