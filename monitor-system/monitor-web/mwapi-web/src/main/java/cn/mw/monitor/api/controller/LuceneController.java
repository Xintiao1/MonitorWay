package cn.mw.monitor.api.controller;

import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.util.lucene.LuceneUtils;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author syt
 * @Date 2021/3/5 17:41
 * @Version 1.0
 */
@RequestMapping("/mwapi")
@Slf4j
@Controller
@Api(value = "索引库", tags = "")
public class LuceneController extends BaseApiService {
    private static final Logger logger = LoggerFactory.getLogger("control-" + LuceneController.class.getName());

    @PostMapping("/lucene/deleteAll")
    @ResponseBody
    @ApiOperation(value = "删除全部索引")
    public ResponseBase deleteAll() {
        // 查询分页
        Reply reply;
        try {
            LuceneUtils.deleteAll();
            reply = Reply.ok();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            logger.error(e.getMessage());
            return setResultFail(e.getMessage(), null);
        }
        return setResultSuccess(reply);
    }
}
