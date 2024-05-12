package cn.mw.monitor.service.model.exception;

import lombok.Data;

@Data
public class NotFindTemplateException extends RuntimeException{
    public NotFindTemplateException(){
        super("没有匹配的模版");
    }
}
