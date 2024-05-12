package cn.mw.monitor.model.control;

import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mw.monitor.model.param.ModelTableViewParam;
import cn.mw.monitor.model.service.MwModelTableViewService;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

/**
 * @author guiquanwnag
 * @datetime 2023/6/30
 * @Description ARP_MAC_IP数据展示
 */
@RequestMapping("/mwapi/tableView")
@Controller
@Slf4j
@Api(value = "ARP_MAC_IP数据展示接口", tags = "ARP_MAC_IP数据展示接口")
public class MwModelTableViewController extends BaseApiService {


    @Value("${model.display.table.view.enable}")
    private Boolean displayEnable;

    @Autowired
    private MwModelTableViewService modelTableViewService;


    @GetMapping("/getEnable")
    @ResponseBody
    @ApiOperation(value = "获取当前系统是否支持展示")
    public ResponseBase getAssetsLabelEnable() {
        Reply reply = null;
        try {
            reply = Reply.ok(displayEnable);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            return setResultFail("获取自定义tab信息是否支持展示失败", "");
        }
        return setResultSuccess(reply);
    }

    @PostMapping("/result/browse")
    @ResponseBody
    @ApiOperation(value = "根据参数获取表格数据")
    public ResponseBase getTableViewResult(@RequestBody ModelTableViewParam param) {
        Reply reply = new Reply();
        try {
            if (displayEnable) {
                reply = modelTableViewService.getTableView(param);
            }
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getTableViewResult error", e);
            return setResultFail("根据参数获取表格数据失败", "");
        }
        return setResultSuccess(reply);
    }

    /**
     * 导出用户数据
     *
     * @param response
     */
    @PostMapping("/result/exportExcel")
    @ApiOperation(value="导出数据")
    @ResponseBody
    public void excelUserExport(@RequestBody ModelTableViewParam param, HttpServletResponse response) {
        try {
            modelTableViewService.exportResultExcel(response,param);
        } catch (Exception e) {
            log.error("excelTemplateExport{}", e);
        }
    }

}
