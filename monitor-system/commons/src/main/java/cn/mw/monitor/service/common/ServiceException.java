package cn.mw.monitor.service.common;

import cn.mwpaas.common.model.Reply;
import java.util.List;

public class ServiceException extends RuntimeException {

    private List<Reply> replyList;

    private Reply reply;

    public ServiceException(List<Reply> replyList){
        this.replyList = replyList;
    }

    public ServiceException(Reply reply) {
        this.reply = reply;
    }

    public List<Reply> getReplyList() {
        return replyList;
    }

    public void setReplyList(List<Reply> replyList) {
        this.replyList = replyList;
    }

    public Reply getReply() {
        return reply;
    }

    public void setReply(Reply reply) {
        this.reply = reply;
    }

    @Override
    public String getMessage() {
        return reply.getMsg();
    }

}
