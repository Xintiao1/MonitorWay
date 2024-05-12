package cn.mw.monitor.user.service;

import cn.mwpaas.common.model.Reply;

import java.util.List;

public interface PasswdRepeatCheck {
    public List<Reply>  repeatCheck(Integer userid, String passwd);
}
