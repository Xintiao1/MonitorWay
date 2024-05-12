package cn.mwpaas.common.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author phzhou
 * @ClassName BusinessException
 * @CreateDate 2018/12/4
 * @Description
 */
@Data
@AllArgsConstructor
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = -60352718859158852L;
    /**
     * 异常状态码
     */
    private Integer code;

    /**
     * 异常信息
     */
    private String message;

    public BusinessException(Integer code, String message, Throwable throwable) {
        super(message, throwable);
        this.code = code;
        this.message = message;
    }
}
