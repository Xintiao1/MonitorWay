package cn.mw.monitor.api.controller;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.knowledgeBase.dto.AddOrUpdateKnowledgeBaseParam;
import cn.mw.monitor.knowledgeBase.dto.DeleteKnowledgeParam;
import cn.mw.monitor.knowledgeBase.dto.QueryKnowledgeBaseParam;
import cn.mw.monitor.knowledgeBase.model.MwKnowledgeUserMapper;
import cn.mw.monitor.knowledgeBase.service.MwKnowledgeBaseService;
import cn.mw.monitor.knowledgeBase.service.RedisService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author syt
 * @Date 2020/8/28 14:41
 * @Version 1.0
 */
@RequestMapping("/mwapi/knowledgeBase")
@Controller
@Api(value = "知识库")
public class MWKnowledgeBaseController extends BaseApiService {
    private static final Logger logger = LoggerFactory.getLogger("control-" + MWKnowledgeBaseController.class.getName());
    @Autowired
    private MwKnowledgeBaseService mwKnowledgeBaseService;
    @Autowired
    private RedisService redisService;

    /**
     * 查询知识库类型树结构
     */
    @MwPermit(moduleName = "knowledge_base")
    @GetMapping("/typeTree/browse")
    @ResponseBody
    @ApiOperation(value = "查询知识库类型（内含各类型知识个数）")
    public ResponseBase getTypeTree(@Param("type") String type) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwKnowledgeBaseService.getTypeTree(type);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("知识库报错", mwKnowledgeBaseService);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("知识库报错", mwKnowledgeBaseService);
        }
        return setResultSuccess(reply);
    }

    /**
     * 查询共享知识库table分页
     */
    @MwPermit(moduleName = "knowledge_base")
    @PostMapping("/browse")
    @ResponseBody
    @ApiOperation(value = "根据条件查询知识table并实现分页")
    public ResponseBase getKnowledgeTableData(@RequestBody QueryKnowledgeBaseParam qParam) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwKnowledgeBaseService.selectTableList(qParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("知识库报错", mwKnowledgeBaseService);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("知识库报错", mwKnowledgeBaseService);
        }
        return setResultSuccess(reply);
    }



    @MwPermit(moduleName = "knowledge_base")
    @PostMapping("/export/instanceListInfo")
    @ResponseBody
    @ApiOperation(value = "导出知识库")
    public void export(@RequestBody QueryKnowledgeBaseParam qParam, HttpServletResponse response) {
        Reply reply;
        try {
            // 验证内容正确性
            mwKnowledgeBaseService.exportTableList(qParam,response);

        } catch (Throwable e) {
            logger.error(e.getMessage());

        }

    }


//
//    /**
//     * 新增知识
//     */
//    @PostMapping("/create")
//    @ResponseBody
//    public ResponseBase addKnowledge(@RequestBody AddOrUpdateKnowledgeBaseParam aParam) {
//        Reply reply;
//        try {
//            // 验证内容正确性
//            reply = mwKnowledgeBaseService.insert(aParam);
//            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
//                return setResultFail(reply.getMsg(), reply.getData());
//            }
//        } catch (Throwable e) {
//            logger.error(e.getMessage());
//            return setResultFail(e.getMessage(), mwKnowledgeBaseService);
//        }
//        return setResultSuccess(reply);
//    }

    /**
     * 根据知识id,获取相应知识内容
     */
    @MwPermit(moduleName = "knowledge_base")
    @PostMapping("/popup/browse")
    @ResponseBody
    public ResponseBase getKnowledgeById(@RequestBody QueryKnowledgeBaseParam qParam) {
        return setResultSuccess("测试成功");
        /*Reply reply;
        try {
            // 验证内容正确性
            reply = mwKnowledgeBaseService.selectById(qParam.getId(),qParam.getGiveFlag());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), mwKnowledgeBaseService);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), mwKnowledgeBaseService);
        }
        return setResultSuccess(reply);*/
    }

    /**
     * 修改共享知识
     */
    @MwPermit(moduleName = "knowledge_base")
    @PostMapping("/editor")
    @ResponseBody
    public ResponseBase updateKnowledge(@RequestBody AddOrUpdateKnowledgeBaseParam uParam) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwKnowledgeBaseService.update(uParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("知识库报错", mwKnowledgeBaseService);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("知识库报错", mwKnowledgeBaseService);
        }
        return setResultSuccess(reply);
    }

    /**
     * 删除共享知识
     */
    @MwPermit(moduleName = "knowledge_base")
    @PostMapping("/delete")
    @ResponseBody
    public ResponseBase deleteKnowledge(@RequestBody DeleteKnowledgeParam dParam) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwKnowledgeBaseService.delete(dParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("知识库报错", mwKnowledgeBaseService);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("知识库报错", mwKnowledgeBaseService);
        }
        return setResultSuccess(reply);
    }

    /**
     * 点赞状态修改
     */
    @MwPermit(moduleName = "knowledge_base")
    @PostMapping("/Redis/Liked/create")
    @ResponseBody
    public ResponseBase saveLikedToRedis(@RequestBody MwKnowledgeUserMapper cParam) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = redisService.saveLikedStatusRedis(cParam.getKnowledgeId(),cParam.getUserId(),cParam.getStatus());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("知识库报错", mwKnowledgeBaseService);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("知识库报错", mwKnowledgeBaseService);
        }
        return setResultSuccess(reply);
    }

    /**
     * 新增个人知识
     */
    @MwPermit(moduleName = "knowledge_base")
    @PostMapping("/mine/create")
    @ResponseBody
    public ResponseBase addMyKnowledge(@RequestBody AddOrUpdateKnowledgeBaseParam aParam) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwKnowledgeBaseService.insertMyKnowledge(aParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("知识库报错", mwKnowledgeBaseService);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("知识库报错", mwKnowledgeBaseService);
        }
        return setResultSuccess(reply);
    }


    @MwPermit(moduleName = "knowledge_base")
    @PostMapping("/export/import")
    @ResponseBody
    @ApiOperation("导入知识库")
    public ResponseBase exportImport(@RequestBody MultipartFile file, HttpServletResponse response) {
        Reply reply;
        try {
            reply = mwKnowledgeBaseService.templateInfoImport(file, response);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("知识库报错", mwKnowledgeBaseService);
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            logger.error("导入数据失败", e);
            return setResultFail("导入数据失败", "");
        }
    }

    /**
     * 修改个人知识
     */
    @MwPermit(moduleName = "knowledge_base")
    @PostMapping("/mine/editor")
    @ResponseBody
    public ResponseBase updateMyKnowledge(@RequestBody AddOrUpdateKnowledgeBaseParam uParam) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwKnowledgeBaseService.updateMyKnowledge(uParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("知识库报错", mwKnowledgeBaseService);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("知识库报错", mwKnowledgeBaseService);
        }
        return setResultSuccess(reply);
    }

    /**
     * 删除个人知识
     */
    @MwPermit(moduleName = "knowledge_base")
    @PostMapping("/mine/delete")
    @ResponseBody
    public ResponseBase deleteMyKnowledge(@RequestBody DeleteKnowledgeParam dParam) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwKnowledgeBaseService.deleteMyKnowledge(dParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("知识库报错", mwKnowledgeBaseService);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("知识库报错", mwKnowledgeBaseService);
        }
        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "knowledge_base")
    @PostMapping("/fuzzSearchAllFiled/browse")
    @ResponseBody
    public ResponseBase scanResultfuzzSearch(@RequestBody QueryKnowledgeBaseParam param,
                                             HttpServletRequest request, RedirectAttributesModelMap model) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwKnowledgeBaseService.fuzzSearchAllFiledData(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail("知识库报错", mwKnowledgeBaseService);
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail("模糊查询所有字段资数据失败", "模糊查询所有字段资数据失败");
        }

        return setResultSuccess(reply);
    }
}
