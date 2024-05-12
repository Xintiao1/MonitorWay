package cn.mw.monitor.user.service;

import cn.mwpaas.common.model.Reply;

import java.util.List;

public interface PasswdComCheck {

    List<Reply> checkComplex(String passwd);

}
