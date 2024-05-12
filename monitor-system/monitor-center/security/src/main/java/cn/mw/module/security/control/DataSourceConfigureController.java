package cn.mw.module.security.control;

import cn.mw.module.security.dto.DataSourceConfigureDTO;
import cn.mw.module.security.service.DataSourceConfigureService;
import cn.mw.monitor.annotation.MwPermit;
import cn.mw.monitor.api.common.BaseApiService;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mwpaas.common.constant.PaasConstant;
import cn.mwpaas.common.model.Reply;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


/**
 * 日志审计  数据源配置页面
 *
 * @author qzg
 * @date 2021/12/08
 */
@RequestMapping("/mwapi/dataSource")
@Controller
@Slf4j
@Api(value = "数据源配置", tags = "数据源配置")
public class DataSourceConfigureController extends BaseApiService {
    @Autowired
    private DataSourceConfigureService dataSourceConfigureService;


    /**
     * 创建数据源连接信息
     *
     * @param param
     * @return
     */

    @MwPermit(moduleName = "log_security")
    @PostMapping("/create")
    @ResponseBody
    public ResponseBase creatDataSourceInfo(@RequestBody DataSourceConfigureDTO param) {
        Reply reply;
        try {
            reply = dataSourceConfigureService.creatDataSourceInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("creatDataSourceInfo {}",e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "log_security")
    @PostMapping("/browse")
    @ResponseBody
    public ResponseBase getDataSourceInfo(@RequestBody DataSourceConfigureDTO param) {
        Reply reply;
        try {
            reply = dataSourceConfigureService.getDataSourceInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getDataSourceInfo {}",e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "log_security")
    @PostMapping("/popup/editor")
    @ResponseBody
    public ResponseBase editorDataSourceInfo(@RequestBody DataSourceConfigureDTO param) {
        Reply reply;
        try {
            reply = dataSourceConfigureService.editorDataSourceInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("editorDataSourceInfo {}",e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }

    @MwPermit(moduleName = "log_security")
    @PostMapping("/delete")
    @ResponseBody
    public ResponseBase deleteDataSourceInfo(@RequestBody DataSourceConfigureDTO param) {
        Reply reply;
        try {
            reply = dataSourceConfigureService.deleteDataSourceInfo(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("deleteDataSourceInfo {}",e);
            return setResultFail(e.getMessage(), param);
        }

        return setResultSuccess(reply);
    }


    /**
     * 获取数据源下拉数据
     *
     * @return
     */
    @MwPermit(moduleName = "log_security")
    @GetMapping("/dropDown")
    @ResponseBody
    public ResponseBase dataSourceDropDown(@Param("type") String type) {
        Reply reply;
        try {
            reply = dataSourceConfigureService.dataSourceDropDown(type);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("dataSourceDropDown {}",e);
            return setResultFail(e.getMessage(), "");
        }

        return setResultSuccess(reply);
    }

    /**
     * 获取数据源页面下拉数据
     *
     * @return
     */
    @MwPermit(moduleName = "log_security")
    @PostMapping("/selectDropDown")
    @ResponseBody
    public ResponseBase dropDownByInsertSelect() {
        Reply reply;
        try {
            reply = dataSourceConfigureService.dropDownByInsertSelect();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("dropDownByInsertSelect {}",e);
            return setResultFail(e.getMessage(), "");
        }

        return setResultSuccess(reply);
    }

    /**
     * 模糊匹配显示提示
     *
     * @return
     */
/*    @MwPermit(moduleName = "log_security")*/
    @PostMapping("/fuzzSearchFiled")
    @ResponseBody
    public ResponseBase fuzzSearchAllFiledData() {
        Reply reply;
        try {
            reply = dataSourceConfigureService.fuzzSearchAllFiledData();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("fuzzSearchAllFiledData {}",e);
            return setResultFail(e.getMessage(), "");
        }

        return setResultSuccess(reply);
    }


    /**
     * 启用数据源配置
     *
     * @return
     */
    @MwPermit(moduleName = "log_security")
    @PostMapping("/initConfig/editor")
    @ResponseBody
    public ResponseBase initConfig(@RequestBody DataSourceConfigureDTO param) {
        Reply reply;
        try {
            reply = dataSourceConfigureService.initConfig(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("initConfig {}",e);
            return setResultFail(e.getMessage(), "");
        }

        return setResultSuccess(reply);
    }

    /**
     * 停用数据源配置
     *
     * @return
     */
    @MwPermit(moduleName = "log_security")
    @PostMapping("/shutDownConfig/editor")
    @ResponseBody
    public ResponseBase shutDownConfig(@RequestBody DataSourceConfigureDTO param) {
        Reply reply;
        try {
            reply = dataSourceConfigureService.shutDownConfig(param);
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("shutDownConfig {}",e);
            return setResultFail(e.getMessage(), "");
        }

        return setResultSuccess(reply);
    }

    /**
     * kafka数据源添加获取topic字段信息
     *
     * @return
     */
    @MwPermit(moduleName = "log_security")//todo ？？作用
    @PostMapping("/getTopicField")
    @ResponseBody
    public ResponseBase getTopicField() {
        Reply reply;
        try {
            reply = dataSourceConfigureService.getTopicField();
            if (null != reply && reply.getRes() != PaasConstant.RES_SUCCESS) {
                return setResultFail(reply.getMsg(), reply.getData());
            }
        } catch (Throwable e) {
            log.error("getTopicField {}",e);
            return setResultFail(e.getMessage(), "");
        }
        return setResultSuccess(reply);
    }
}
