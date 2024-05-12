package cn.mw.monitor.websocket;

import lombok.Data;

import java.util.Date;

/**
 * @author lumingming
 * @createTime 202110/2929 10:56
 * @description lmm
 */
@Data
public class Message {
    //消息内容
    private int id;
    //消息内容
    private String messageText;
    //属于哪个分类
    private String module;
    //时间
    private Date createDate;
    //所属用户
    private String ownUser;
    //所属用户id
    private Integer userId;
    //是否已读
    private Integer readStatus;
    //是否跳转
    private Boolean isRedirect;
    //跳转信息
    private Object node;

}
