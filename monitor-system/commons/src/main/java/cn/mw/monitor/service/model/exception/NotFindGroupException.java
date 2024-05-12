package cn.mw.monitor.service.model.exception;

import lombok.Data;

@Data
public class NotFindGroupException extends RuntimeException{
    public NotFindGroupException(){
        super("没有匹配的主机群组");
    }
}
