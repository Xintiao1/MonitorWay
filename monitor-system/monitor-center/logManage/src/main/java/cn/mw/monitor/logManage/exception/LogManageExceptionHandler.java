package cn.mw.monitor.logManage.exception;

import cn.mw.monitor.api.common.Constants;
import cn.mw.monitor.api.common.ResponseBase;
import cn.mwpaas.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@ControllerAdvice
public class LogManageExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(value = LogManageBusinessException.class)
    @ResponseBody
    public ResponseBase businessExceptionHandler(HttpServletRequest request, BusinessException exception) {
        log.error("发生业务异常！URL是：{}", request.getRequestURI());
        log.error("发生业务异常！原因是：", exception);
        return setResultFail(exception.getMessage());
    }

    public ResponseBase setResultFail(String msg) {
        return new ResponseBase(Constants.HTTP_RES_CODE_500, msg, null);
    }
}
