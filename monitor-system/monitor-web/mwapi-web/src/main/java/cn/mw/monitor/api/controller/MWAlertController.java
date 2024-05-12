package cn.mw.monitor.api.controller;


import cn.mw.monitor.screen.param.MWNewScreenAssetsCensusParam;
import cn.mw.monitor.service.alert.dto.AlertReasonEditorParam;
import cn.mw.monitor.service.user.param.LoginParam;
import cn.mw.monitor.service.zbx.param.*;
import cn.mw.monitor.service.alert.api.MWAlertService;
import cn.mw.monitor.service.alert.dto.MWItemDto;
import cn.mw.monitor.service.alert.dto.RecordParam;
import cn.mw.monitor.util.DingdingQunSendUtil;
import cn.mw.monitor.util.RSAUtils;
import cn.mw.monitor.util.RedisUtils;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import cn.mw.monitor.alert.param.LuceneParam;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.zbx.manger.MWWebZabbixManger;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.server.PathParam;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xhy
 * @date 2020/3/26 23:05
 */
@RequestMapping("/mwapi/alert")
@Controller
@Api(value = "活动告警")
@Slf4j(topic = "MWAlertController")
public class MWAlertController extends BaseApiService {
    @Autowired
    private MWAlertService mwalertService;
    @Autowired
    MWWebZabbixManger mwWebZabbixManger;
    @Autowired
    cn.mw.monitor.accountmanage.dao.MwAlerthistory7daysTableDao mwAlerthistory7daysTableDao;
    @Autowired
    private RedisUtils redisUtils;
    private String key = "alert-custom-time";

    @Value("${alert.level}")
    private String alertLevel;

    @Value("${server.port}")
    private Integer port;

    private String TAI_SHENG_KEY = "tai";


    /**
     * 查询当前报警
     */
    @PostMapping("/now/browse")
    @ResponseBody
    @ApiOperation(value = "查询当前报警")
    public ResponseBase getNowAlert(@RequestBody AlertParam alertParam) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwalertService.getCurrAlertPage(alertParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getNowAlert",e);
            return setResultFail("查询告警报错!", mwalertService);
        }
        return setResultSuccess(reply);


    }

    /**
     * 查询历史报警
     */
    @PostMapping("/hist/browse")
    @ResponseBody
    @ApiOperation(value = "查询历史报警")
    public ResponseBase getHistList(@RequestBody AlertParam alertParam) {

        Reply reply;
        try {
            // 验证内容正确性
            reply = mwalertService.getHistAlertPage(alertParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getHistList_case{}",alertParam,e);
            return setResultFail("查询历史告警报错!", mwalertService);
        }

        return setResultSuccess(reply);

    }

    @PostMapping("/now/fuzzSeachAllFiled/browse")
    @ResponseBody
    @ApiOperation(value = "当前告警模糊查询")
    public ResponseBase nowFuzzSeachAllFiledData(@RequestBody AlertParam alertParam) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwalertService.nowFuzzSeachAllFiledData(alertParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("当前告警模糊查询报错!", "模糊查询所有字段资数据失败");
        }

        return setResultSuccess(reply);
    }

    @PostMapping("/hist/fuzzSeachAllFiled/browse")
    @ResponseBody
    @ApiOperation(value = "历史告警模糊查询")
    public ResponseBase histFuzzSeachAllFiledData(@RequestBody AlertParam alertParam) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwalertService.histFuzzSeachAllFiledData(alertParam);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage());
            return setResultFail("历史告警模糊查询报错！", "模糊查询所有字段资数据失败");
        }

        return setResultSuccess(reply);
    }

    /**
     * 导出历史报警
     */
    @PostMapping("/histexport/perform")
    @ResponseBody
    @ApiOperation(value = "导出历史报警")
    public void histExport(@RequestBody AlertParam alertParam, HttpServletResponse response) {
        try {
            // 导出
            mwalertService.export(alertParam,response);
        } catch (Throwable e) {
            log.error("histexport{}",e);
            //return setResultFail(e.getMessage(), mwalertService);
        }
        //return setResultSuccess();

    }

    @PostMapping("/getHistByEventId/browse")
    @ResponseBody
    @ApiOperation(value = "查询告警历史")
    public ResponseBase getHistByEventId(@RequestBody AlertParam alertParam) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwalertService.getAlertHistory(alertParam.getMonitorServerId(), alertParam.getObjectId());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getHistByEventId{}",alertParam,e);
            return setResultFail("查询告警历史报错！", mwalertService);
        }

        return setResultSuccess(reply.getData());
    }

    @PostMapping("/getDetailsByEventId/browse")
    @ResponseBody
    @ApiOperation(value = "查询告警详情")
    public ResponseBase getDetailsByEventId(@RequestBody AlertParam alertParam) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwalertService.getNoticeList(alertParam.getMonitorServerId(), alertParam.getEventid());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getDetailsByEventId{}",alertParam,e);
            return setResultFail("查询告警详情报错!", mwalertService);
        }

        return setResultSuccess(reply.getData());
    }


    /**
     * 确认事件
     *
     * @param eventid
     * @return
     */
    @GetMapping("/confirm/perform")
    @ResponseBody
    @ApiOperation(value = "确认事件")
    public ResponseBase confirm(@PathParam("monitorServerId") Integer monitorServerId, @PathParam("userId") Integer userId, @PathParam("eventid") String eventid) {

        Reply reply;
        try {
            // 验证内容正确性
            reply = mwalertService.confirm(monitorServerId, userId, eventid, "qr");
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("confirm{}",e);
            return setResultFail("确认事件报错！", mwalertService);
        }

        return setResultSuccess(reply);
    }

    /**
     * 确认事件
     *
     * @param param
     * @return
     */
    @PostMapping("/confirmList/perform")
    @ResponseBody
    @ApiOperation(value = "批量确认事件")
    public ResponseBase confirmList(@RequestBody List<ConfirmDto> param) {

        Reply reply;
        try {
            // 验证内容正确性
            reply = mwalertService.confirmList(param, "qr");
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("confirm{}",e);
            return setResultFail("批量确认事件报错!", mwalertService);
        }

        return setResultSuccess(reply);
    }


    /**
     * 关闭处理
     *
     * @param eventid
     * @return
     */
    @GetMapping("/close/perform")
    @ResponseBody
    @ApiOperation(value = "关闭处理")
    public ResponseBase closeAlert(@PathParam("monitorServerId") Integer monitorServerId, @PathParam("userId") Integer userId, @PathParam("eventid") String eventid) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwalertService.confirm(monitorServerId, userId, eventid, "cl");
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("closeAlert{}",e);
            return setResultFail("关闭处理报错!", mwalertService);
        }

        return setResultSuccess(reply);
    }

    /**
     * 通过objectid查询itemid和itemname
     *
     * @param objectid
     * @return
     */
    @GetMapping("/getItemByTriggerId")
    @ResponseBody
    @ApiOperation(value = "查询itemnname")
    public ResponseBase getItemByTriggerId(@PathParam("monitorServerId") Integer monitorServerId, @PathParam("objectid") String objectid) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwalertService.getItemByTriggerId(monitorServerId, objectid);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getItemByTriggerId{}",e);
            return setResultFail("查询itemnname报错!", mwalertService);
        }

        return setResultSuccess(reply);
    }

    /**
     * 通过itemid查询历史数据
     *
     * @param mwItemDto
     * @return
     */
    @PostMapping("/getHistoryByItemId")
    @ResponseBody
    @ApiOperation(value = "查询item的前后1小时历史数据")
    public ResponseBase getHistoryByItemId(@RequestBody MWItemDto mwItemDto) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwalertService.getHistoryByItemId(mwItemDto);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getHistoryByItemId{}",e);
            return setResultFail("查询item的前后1小时历史数据报错!", mwalertService);
        }

        return setResultSuccess(reply);
    }

    /**
     * 根据告警标题查询知识库
     *
     * @param title
     * @return
     */
    @PostMapping("/getLuceneByTitle")
    @ResponseBody
    @ApiOperation(value = "根据告警标题查询知识库")
    public ResponseBase getLuceneByTitle(@RequestBody LuceneParam title) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwalertService.getLuceneByTitle(title.getTitle());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getLuceneByTitle{}",e);
            return setResultFail("根据告警标题查询知识库报错!", mwalertService);
        }

        return setResultSuccess(reply);
    }
    /**
     * 获取发送信息历史记录
     *
     * @param
     * @return
     */
    @PostMapping("/getSendInfo")
    @ResponseBody
    @ApiOperation(value = "获取发送信息历史记录")
    public ResponseBase getSendInfo(@RequestBody RecordParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwalertService.getSendInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getSendInfo{}",e);
            return setResultFail("获取发送信息历史记录报错!", mwalertService);
        }

        return setResultSuccess(reply);
    }

    @PostMapping("/getAlertLevel")
    @ResponseBody
    @ApiOperation(value = "获取告警等级")
    public ResponseBase getAlertLevel() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwalertService.getAlertLevel();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAlertLevel{}",e);
            return setResultFail("获取告警等级报错!", mwalertService);
        }

        return setResultSuccess(reply);
    }

    @PostMapping("/reason/editor")
    @ResponseBody
    @ApiOperation(value = "告警信息原因编辑")
    public ResponseBase reasonEditor(@RequestBody AlertReasonEditorParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwalertService.reasonEditor(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAlertLevel{}",e);
            return setResultFail("告警信息原因编辑报错!", mwalertService);
        }

        return setResultSuccess(reply);
    }

    @PostMapping("/close/trigger")
    @ResponseBody
    @ApiOperation(value = "关闭告警触发器")
    public ResponseBase closeEventId(@RequestBody List<CloseDto> param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwalertService.closeEventId(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAlertLevel{}",e);
            return setResultFail("关闭告警触发器报错!", mwalertService);
        }

        return setResultSuccess(reply);
    }

    @PostMapping("/get/trigger")
    @ResponseBody
    @ApiOperation(value = "获取关闭告警触发器")
    public ResponseBase getTiegger() {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwalertService.getTiegger();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAlertLevel{}",e);
            return setResultFail("获取关闭告警触发器报错！", mwalertService);
        }

        return setResultSuccess(reply);
    }

    @GetMapping("/custom/time")
    @ResponseBody
    @ApiOperation(value = "自定义刷新时间")
    public ResponseBase customTime(@PathParam("num") Integer num) {

        try {
            redisUtils.set(key,num);
        } catch (Throwable e) {
            log.error("getAlertLevel{}",e);
            return setResultFail("自定义刷新时间报错!", "customTime");
        }

        return setResultSuccess("添加成功！");
    }

    @GetMapping("/get/time")
    @ResponseBody
    @ApiOperation(value = "获取自定义刷新时间")
    public ResponseBase getTime() {
        Integer num = 0;
        try{
            if(redisUtils.hasKey(key)){
                num = Integer.parseInt(redisUtils.get(key).toString());
            }
        }catch (Exception e){
            log.error("getTime{}",e);
            return setResultFail("获取自定义刷新时间报错!", "getTime");
        }
        return setResultSuccess(num);
    }

    @GetMapping("/get/project")
    @ResponseBody
    @ApiOperation(value = "获取当前项目环境")
    public ResponseBase getProject() {
        return setResultSuccess(alertLevel);
    }

    @PostMapping("/ignore/alert")
    @ResponseBody
    @ApiOperation(value = "忽略告警")
    public ResponseBase ignoreAlert(@RequestBody List<IgnoreAlertDto> params) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwalertService.ignoreAlert(params);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAlertLevel{}",e);
            return setResultFail("忽略告警报错！", mwalertService);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/getignore/alert")
    @ResponseBody
    @ApiOperation(value = "查询告警")
    public ResponseBase getignoreAlert(IgnoreAlertDto param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwalertService.getignoreAlert(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getAlertLevel{}",e);
            return setResultFail("查询告警报错！", mwalertService);
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/triggerexport/perform")
    @ResponseBody
    @ApiOperation(value = "导出历史报警")
    public void triggerExport(HttpServletResponse response) {
        try {
            // 导出
            mwalertService.triggerExport(response);
        } catch (Throwable e) {
            log.error("histexport{}",e);
            //return setResultFail(e.getMessage(), mwalertService);
        }
        //return setResultSuccess();

    }

    @PostMapping("/getAlertCount")
    @ResponseBody
    public ResponseBase getAlertCount(@RequestBody AlertCountParam param) {
        Reply reply;
        try {
            reply = mwalertService.getAlertCount(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("获取告警级数量错误", e);
            return setResultFail("获取告警级数量错误", "");
        }
    }

    @PostMapping("/getAlert")
    @ResponseBody
    public ResponseBase getAlert(@RequestBody AlertCountParam param) {
        Reply reply;
        try {
            reply = mwalertService.getAlert(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
            return setResultSuccess(reply);
        } catch (Exception e) {
            log.error("获取告警级数量错误", e);
            return setResultFail("获取告警级数量错误", "");
        }
    }

    @PostMapping("/getEventFlowByEventId")
    @ResponseBody
    @ApiOperation(value = "查询事件闭环")
    public ResponseBase getEventFlowByEventId(@RequestBody AlertParam alertParam) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwalertService.getEventFlowByEventId(alertParam.getMonitorServerId(), alertParam.getEventid());
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("查询事件闭环错误:{}",alertParam,e);
            return setResultFail("查询事件闭环错误", mwalertService);
        }

        return setResultSuccess(reply.getData());
    }

    @PostMapping("/getAlertMessage")
    @ResponseBody
    @ApiOperation(value = "查询告警消息")
    public ResponseBase getAlertMessage(@RequestBody AlertCountParam param) {
        Reply reply;
        try {
            // 验证内容正确性
            reply = mwalertService.getAlertMessage(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("查询事件闭环错误:{}",e);
            return setResultFail("查询事件闭环错误", mwalertService);
        }

        return setResultSuccess(reply.getData());
    }

    @PostMapping("open/getToken")
    @ResponseBody
    @ApiOperation(value = "根据用户查询token")
    public ResponseBase getToken(@Validated @RequestBody LoginParam loginParam) throws IOException {
        //私钥
        String privateKey = RSAUtils.RSA_PRIVATE_KEY;
        //用私钥解密后的用户名和密码
        String loginNameReal = RSAUtils.decryptData(loginParam.getLoginName(),privateKey);
        String key = TAI_SHENG_KEY + "_" + loginNameReal;
        String token = null;
        if(redisUtils.hasKey(key)){
            token = redisUtils.get(key).toString();
            return setResultSuccess(token);
        }
        String url = "http://localhost:" + port + "/mwapi/user/login";
        Map<String, Object> param = new HashMap<>();
        param.put("loginName",loginParam.getLoginName());
        param.put("loginType",loginParam.getLoginType());
        param.put("password",loginParam.getPassword());
        String result = DingdingQunSendUtil.doPost(url,param,null);
        JSONObject json = JSONArray.parseObject(result);
        token = json.getJSONObject("data").get("token").toString();
        redisUtils.set(key,token);
        return setResultSuccess(token);
    }

    @GetMapping("open/saveToken")
    @ResponseBody
    @ApiOperation(value = "存储用户token")
    public ResponseBase saveToken(@RequestParam String userName,@RequestParam String token) {
        String key = TAI_SHENG_KEY + "_" + userName;
        redisUtils.set(key,token);
        return setResultSuccess();
    }

}
